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
package com.fortify.integration.sonarqube.ssc.sq67.scanner;

import java.io.Closeable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.IndexTreeList;
import org.mapdb.Serializer;
import org.sonar.api.PropertyType;
import org.sonar.api.Startable;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinition;

import com.fortify.integration.sonarqube.ssc.common.scanner.AbstractFortifyIssueSensorProperties;
import com.fortify.integration.sonarqube.ssc.common.scanner.FortifyIssueSensorHelper;
import com.fortify.integration.sonarqube.ssc.common.scanner.FortifyIssueSensorHelper.FORTIFY_FIELDS;
import com.fortify.integration.sonarqube.ssc.common.scanner.IFortifyScannerSideConnectionHelper;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.processor.AbstractJSONMapProcessor;

/**
 * This {@link FortifySQ67AbstractSensor} implementation retrieves vulnerability data from SSC and
 * reports these vulnerabilities as SonarQube issues.
 * 
 * TODO Add more JavaDoc
 * 
 * @author Ruud Senden
 *
 */

/*
 * TODO Add plugin page that shows any Fortify issues that could not be matched to a SonarQube source file
 */
public class FortifySQ67IssueSensor extends FortifySQ67AbstractSensor implements Startable {
	private final SensorProperties sensorProperties;
	private final CacheHelper cacheHelper;
	
	/**
	 * Constructor for injecting dependencies
	 * @param connFactory
	 */
	public FortifySQ67IssueSensor(IFortifyScannerSideConnectionHelper connHelper, SensorProperties sensorProperties) {
		super(connHelper);
		this.sensorProperties = sensorProperties;
		this.cacheHelper = new CacheHelper(sensorProperties);
	}
	
	@Override
	public void describe(SensorDescriptor descriptor) {
		descriptor.name("Fortify issue collection");
	}
	
	/**
	 * Sensor implementation that retrieves issue details from SSC, and reports them as SonarQube violations
	 */
	@Override
	public void _execute(SensorContext context) {
		new FortifyIssuesProcessor(context, getConnHelper(), sensorProperties, cacheHelper).processIssues();
	}

	/**
	 * @param context
	 * @return true if SSC connection is available and issue collection is enabled, false otherwise
	 */
	@Override
	protected final boolean isActive(SensorContext context) {
		return sensorProperties.isIssueCollectionEnabled(context);
	}
	
	@Override
	public void start() {
		// Nothing to do
	}

	@Override
	public void stop() {
		cacheHelper.close();
	}
	
	@ScannerSide
	@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
	public static final class SensorProperties extends AbstractFortifyIssueSensorProperties {
		private static final String PRP_REPORT_ISSUES_ONCE = "sonar.fortify.issues.reportOnce";
		
		public SensorProperties(IFortifyScannerSideConnectionHelper connHelper) {
			super(connHelper);
		}
		
		public final boolean isReportIssuesOnce(SensorContext context) {
			return context.config().getBoolean(PRP_REPORT_ISSUES_ONCE).orElse(true);
		}
		
		public static final void addPropertyDefinitions(List<PropertyDefinition> propertyDefinitions) {
			propertyDefinitions.add(PropertyDefinition.builder(PRP_REPORT_ISSUES_ONCE)
					.name("Report issues only once")
					.description("Fortify issues are mapped to source files based on relative paths. As such, a single"
							+ " Fortify issue could be incorrectly mapped to similarly named source files in different modules."
							+ " If this property is set to false (default), the Fortify issue will be reported on"
							+ " every matching source file. If this property is set to true, the Fortify issue will"
							+ " be reported at most once. In either case, the Fortify issue may be reported on"
							+ " incorrect file(s)")
					.type(PropertyType.BOOLEAN)
					.defaultValue("false")
					.build());
		}
	}
	
	private static final class CacheHelper implements Closeable {
		private final SensorProperties sensorProperties;
		private final HashMap<String, IndexTreeList<JSONMap>> issuesByCategory = new HashMap<>();
		private final HashSet<String> reportedIssues = new HashSet<>();
		private DB db = null;
		
		public CacheHelper(SensorProperties sensorProperties) {
			this.sensorProperties = sensorProperties;
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

		public HashMap<String, IndexTreeList<JSONMap>> getIssuesByCategory() {
			return issuesByCategory;
		}

		public boolean ignoreIssue(SensorContext context, ActiveRule activeRule, JSONMap issue) {
			return sensorProperties.isReportIssuesOnce(context) 
					? reportedIssues.contains(getProcessedIssueKey(activeRule, issue))
					: false;
		}

		public void addProcessedIssue(SensorContext context, ActiveRule activeRule, JSONMap issue) {
			if ( sensorProperties.isReportIssuesOnce(context) ) {
				reportedIssues.add(getProcessedIssueKey(activeRule, issue));
			}
		}
		
		private String getProcessedIssueKey(ActiveRule activeRule, JSONMap issue) {
			return activeRule.ruleKey()+"."+FORTIFY_FIELDS.id.get(issue, String.class);
		}
	}
	
	private static final class FortifyIssuesProcessor extends FortifyIssueSensorHelper.AbstractFortifyIssuesProcessor<SensorProperties> {
		private final CacheHelper cacheHelper;
		
		public FortifyIssuesProcessor(SensorContext context, IFortifyScannerSideConnectionHelper connHelper, SensorProperties sensorProperties, CacheHelper cacheHelper) {
			super(context, connHelper, sensorProperties);
			this.cacheHelper = cacheHelper;
		}

		@Override
		public void processAllIssues(ActiveRule activeRule) {
			FortifyIssueProcessor processor = new FortifyIssueProcessor(getContext(), activeRule, getInputFiles(), cacheHelper);
			getAllIssues().forEach(issue -> processor.process(issue) );
		}
		
		@Override
		public void processIssuesForExternalCategory(ActiveRule activeRule, String externalListId, String externalCategory) {
			FortifyIssueProcessor processor = new FortifyIssueProcessor(getContext(), activeRule, getInputFiles(), cacheHelper);
			getIssuesForExternalCategory(externalListId, externalCategory).forEach(issue -> processor.process(issue) );
		}
		
		@SuppressWarnings("unchecked")
		private synchronized IndexTreeList<JSONMap> getAllIssues() {
			return cacheHelper.getIssuesByCategory().computeIfAbsent("all", key -> {
				IndexTreeList<JSONMap> result = (IndexTreeList<JSONMap>)cacheHelper.getDB().<JSONMap>indexTreeList(key, Serializer.JAVA).create();
				// TODO With SQ 6.7 we can only process static analysis results (we can't report project-level issues), so we should add corresponding filters to the base issue query.
				getIssuesBaseQuery().build().processAll(new AbstractJSONMapProcessor() {
					@Override
					public void process(JSONMap json) {
						result.add(json);						
					}
				});
				return result;
			});
		}
		
		@SuppressWarnings("unchecked")
		private synchronized IndexTreeList<JSONMap> getIssuesForExternalCategory(String externalListId, String externalCategory) {
			return cacheHelper.getIssuesByCategory().computeIfAbsent(externalListId+"."+externalCategory, key -> {
				System.out.println("Cache miss getIssuesForExternalCategory - "+key);
				IndexTreeList<JSONMap> result = (IndexTreeList<JSONMap>)cacheHelper.getDB().<JSONMap>indexTreeList(key, Serializer.JAVA).create();
				getIssuesBaseQuery().build().processAll(new AbstractJSONMapProcessor() {
					@Override
					public void process(JSONMap json) {
						result.add(json);						
					}
				});
				return result;
			});
		}
	}
	
	private static final class FortifyIssueProcessor extends FortifyIssueSensorHelper.AbstractFortifyIssueProcessor {
		private final CacheHelper cacheHelper;
		public FortifyIssueProcessor(SensorContext context, ActiveRule activeRule, List<InputFile> inputFiles, CacheHelper cacheHelper) {
			super(context, activeRule, inputFiles);
			this.cacheHelper = cacheHelper;
		}
		
		@Override
		protected boolean ignoreIssue(JSONMap issue) {
			return cacheHelper.ignoreIssue(getContext(), getActiveRule(), issue);
		}
		
		@Override
		protected void createIssueOnInputFile(InputFile inputFile, JSONMap issue) {
			super.createIssueOnInputFile(inputFile, issue);
			cacheHelper.addProcessedIssue(getContext(), getActiveRule(), issue);
		}
	}
}