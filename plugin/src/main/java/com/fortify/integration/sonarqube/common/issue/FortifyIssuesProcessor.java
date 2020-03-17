/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
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
import java.util.HashSet;

import org.apache.commons.collections.CollectionUtils;
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

public class FortifyIssuesProcessor {
	private static final Logger LOG = Loggers.get(FortifyIssuesProcessor.class);
	private final IFortifySourceSystemIssueQueryHelper issueQueryHelper;
	private final IFortifyIssueJSONMapProcessorFactory issueProcessorFactory;
	private final CacheHelper cacheHelper;
	
	public FortifyIssuesProcessor(IFortifySourceSystemIssueQueryHelper issueQueryHelper, IFortifyIssueJSONMapProcessorFactory issueProcessorFactory) {
		this(issueQueryHelper, issueProcessorFactory, null);
	}
	
	public FortifyIssuesProcessor(IFortifySourceSystemIssueQueryHelper issueQueryHelper, IFortifyIssueJSONMapProcessorFactory issueProcessorFactory, CacheHelper cacheHelper) {
		this.issueQueryHelper = issueQueryHelper;
		this.issueProcessorFactory = issueProcessorFactory;
		this.cacheHelper = cacheHelper;
	}
	
	public final void processIssues(SensorContext context) {
		IFortifyIssueInputFileRetriever issueInputFileRetriever = new FortifyIssueInputFileRetrieverPathBased(context);
		ExternalList externalList = FortifyRulesDefinition.getExternalList();
		if ( externalList==null ) {
			processIssuesWithoutExternalList(context, issueInputFileRetriever);
		} else {
			processIssuesWithExternalList(context, externalList, issueInputFileRetriever);
		}
	}

	private final void processIssuesWithExternalList(SensorContext context, ExternalList externalList, IFortifyIssueInputFileRetriever issueInputFileRetriever) {
		LOG.debug("External list configured, mapping issues against corresponding external list category rules"); 
		// Check if there are any active Fortify rules, otherwise no need to process issues
		if ( CollectionUtils.isNotEmpty(context.activeRules().findByRepository(FortifyRulesDefinition.REPOSITORY_KEY)) ) {
			processIssues(context, new FortifyIssueRuleKeysRetrieverExternalList(context, externalList), issueInputFileRetriever);
		}
	}

	private final void processIssuesWithoutExternalList(SensorContext context, IFortifyIssueInputFileRetriever issueInputFileRetriever) {
		LOG.debug("No external list configured, mapping all issues against single Fortify rule"); 
		ActiveRule activeRule = context.activeRules().find(RuleKey.of(FortifyRulesDefinition.REPOSITORY_KEY, FortifyRulesDefinition.RULE_KEY_OTHER));
		if ( activeRule!=null ) {
			processIssues(context, new FortifyIssueRuleKeysRetrieverSingleRule(activeRule), issueInputFileRetriever);
		}
	}
	
	private final void processIssues(SensorContext context, IFortifyIssueRuleKeysRetriever issueRuleKeyRetriever, IFortifyIssueInputFileRetriever issueInputFileRetriever) {
		IJSONMapProcessor processor = issueProcessorFactory.getProcessor(context, issueRuleKeyRetriever, issueInputFileRetriever, cacheHelper);
		if ( cacheHelper==null ) {
			issueQueryHelper.getAllIssuesQuery().processAll(processor);
		} else {
			cacheHelper.processIssues(issueQueryHelper.getAllIssuesQuery(), processor);
		}
	}
	
	public static final class CacheHelper implements Closeable {
		private final IFortifySourceSystemIssueFieldRetriever issueFieldRetriever;
		private final boolean ignorePreviousReportedIssues;
		private final HashSet<String> reportedIssueIds = new HashSet<>();
		private DB db = null;
		private IndexTreeList<JSONMap> issues;
		
		public CacheHelper(IFortifySourceSystemIssueFieldRetriever issueFieldRetriever, boolean ignorePreviouslyReportedIssues) {
			this.issueFieldRetriever = issueFieldRetriever;
			this.ignorePreviousReportedIssues = ignorePreviouslyReportedIssues;
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
		public synchronized void processIssues(IRestConnectionQuery queryIfAbsent, IJSONMapProcessor processor) {
			if ( issues!=null ) {
				issues.forEach(processor::process);
			} else {
				issues = (IndexTreeList<JSONMap>)getDB().<JSONMap>indexTreeList("issues", Serializer.JAVA).create();
				queryIfAbsent.processAll(new AbstractJSONMapProcessor() {
					@Override
					public void process(JSONMap json) {
						issues.add(json);
						processor.process(json);
					}
				});
			}
		}

		public boolean ignoreIssue(JSONMap issue) {
			return ignorePreviousReportedIssues && reportedIssueIds.contains(getProcessedIssueId(issue));
		}

		public void addProcessedIssue(SensorContext context, JSONMap issue) {
			if ( ignorePreviousReportedIssues ) { reportedIssueIds.add(getProcessedIssueId(issue)); }
		}
		
		private String getProcessedIssueId(JSONMap issue) {
			return issueFieldRetriever.getId(issue);
		}
	}
}
