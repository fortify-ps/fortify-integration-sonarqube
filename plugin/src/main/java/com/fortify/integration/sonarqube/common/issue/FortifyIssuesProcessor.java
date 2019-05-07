/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.integration.sonarqube.common.issue;

import java.io.Closeable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.IndexTreeList;
import org.mapdb.Serializer;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.integration.sonarqube.common.externalmetadata.ExternalList;
import com.fortify.integration.sonarqube.common.rule.FortifyRulesDefinition;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.processor.AbstractJSONMapProcessor;
import com.fortify.util.rest.json.processor.IJSONMapProcessor;
import com.fortify.util.rest.query.IRestConnectionQuery;

public class FortifyIssuesProcessor implements Closeable {
	private static final Logger LOG = Loggers.get(FortifyIssuesProcessor.class);
	private final IFortifySourceSystemIssueQueryHelper issueQueryHelper;
	private final IFortifyIssueJSONMapProcessorFactory issueProcessorFactory;
	private final CacheHelper cacheHelper;
	
	public FortifyIssuesProcessor(IFortifySourceSystemIssueQueryHelper issueQueryHelper, IFortifyIssueJSONMapProcessorFactory issueProcessorFactory, boolean useCache) {
		this.issueQueryHelper = issueQueryHelper;
		this.issueProcessorFactory = issueProcessorFactory;
		this.cacheHelper = useCache ? new CacheHelper(issueProcessorFactory.getIssueFieldRetriever()) : null;
	}
	
	public final void processIssues(SensorContext context) {
		ExternalList externalList = FortifyRulesDefinition.getExternalList();
		if ( externalList==null ) {
			processAllIssues(context);
		} else {
			if ( !issueQueryHelper.supportsExternalList(externalList) ) {
				// TODO; get all issues and map the fortify category to the external list rule, instead of just adding all issues to the 'Other' rule
				processAllIssues(context);
			} else {
				processIssuesForExternalList(context, externalList);
			}
		}
	}

	private final void processIssuesForExternalList(SensorContext context, ExternalList externalList) {
		LOG.debug("External list configured, mapping issues against corresponding external list category rules"); 
		Set<String> availableExternalCategories = issueQueryHelper.getAvailableExternalCategories(externalList);
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Available external categories on source system: "+availableExternalCategories);
		}
		for ( ActiveRule activeRule : context.activeRules().findByRepository(FortifyRulesDefinition.REPOSITORY_KEY) ) {
			String externalCategory = activeRule.internalKey();
			LOG.debug("Processing rule for external category: "+externalCategory);
			if ( !availableExternalCategories.contains(externalCategory) ) {
				LOG.debug("External category "+externalCategory+" not available on source system");
			} else {
				LOG.debug("Processing issues for external category "+externalCategory);
				processIssues(context, activeRule, issueQueryHelper.getExternalCategoryIssuesQuery(externalList, externalCategory));
			}
		}
	}

	private final void processAllIssues(SensorContext context) {
		LOG.debug("No external list configured, mapping all issues against single Fortify rule"); 
		ActiveRule activeRule = context.activeRules().find(RuleKey.of(FortifyRulesDefinition.REPOSITORY_KEY, FortifyRulesDefinition.RULE_KEY_OTHER));
		if ( activeRule!=null ) {
			processIssues(context, activeRule, issueQueryHelper.getAllIssuesQuery());
		}
	}
	
	private final void processIssues(SensorContext context, ActiveRule activeRule, IRestConnectionQuery query) {
		IJSONMapProcessor processor = issueProcessorFactory.getProcessor(context, activeRule, cacheHelper);
		if ( cacheHelper==null ) {
			query.processAll(processor);
		} else {
			cacheHelper.getIssues(activeRule, query).forEach(processor::process);
		}
	}
	
	/**
	 * This method must be called to properly close the cache, if constructor was invoked with useCache==true
	 */
	@Override
	public void close() {
		if ( cacheHelper!=null ) {
			cacheHelper.close();
		}
	}
	
	public static final class CacheHelper implements Closeable {
		private final IFortifySourceSystemIssueFieldRetriever issueFieldRetriever;
		private final HashMap<String, IndexTreeList<JSONMap>> issuesByRuleKey = new HashMap<>();
		private final HashSet<String> reportedIssueIds = new HashSet<>();
		private DB db = null;
		
		public CacheHelper(IFortifySourceSystemIssueFieldRetriever issueFieldRetriever) {
			this.issueFieldRetriever = issueFieldRetriever;
		}

		public synchronized DB getDB() {
			if ( db==null ) {
				db = DBMaker.tempFileDB()
						.closeOnJvmShutdown().fileDeleteAfterClose()
						.fileMmapEnableIfSupported()
						.make();
			}
			return db;
		}
		
		@Override
		public void close() {
			if ( db!=null ) {
				db.close();
			}
		}

		@SuppressWarnings("unchecked")
		public IndexTreeList<JSONMap> getIssues(ActiveRule activeRule, IRestConnectionQuery queryIfAbsent) {
			return issuesByRuleKey.computeIfAbsent(activeRule.ruleKey().toString(), key -> {
				IndexTreeList<JSONMap> result = (IndexTreeList<JSONMap>)getDB().<JSONMap>indexTreeList(key, Serializer.JAVA).create();
				queryIfAbsent.processAll(new AbstractJSONMapProcessor() {
					@Override
					public void process(JSONMap json) {
						result.add(json);						
					}
				});
				return result;
			});
		}

		public boolean hasProcessedIssue(ActiveRule activeRule, JSONMap issue) {
			return reportedIssueIds.contains(getProcessedIssueId(activeRule, issue));
		}

		public void addProcessedIssue(SensorContext context, ActiveRule activeRule, JSONMap issue) {
			reportedIssueIds.add(getProcessedIssueId(activeRule, issue));
		}
		
		private String getProcessedIssueId(ActiveRule activeRule, JSONMap issue) {
			return activeRule.ruleKey()+"."+issueFieldRetriever.getId(issue);
		}
	}
}
