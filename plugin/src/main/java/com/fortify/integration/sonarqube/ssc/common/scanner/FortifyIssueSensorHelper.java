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
package com.fortify.integration.sonarqube.ssc.common.scanner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.client.ssc.api.SSCIssueAPI;
import com.fortify.client.ssc.api.SSCIssueGroupsAPI;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionIssueGroupsQueryBuilder;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionIssuesQueryBuilder;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionIssuesQueryBuilder.QueryMode;
import com.fortify.integration.sonarqube.ssc.common.FortifyConstants;
import com.fortify.integration.sonarqube.ssc.common.rule.FortifyRulesDefinition;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.processor.AbstractJSONMapProcessor;

public class FortifyIssueSensorHelper {
	public static enum FORTIFY_FIELDS {
		id, deepLink, engineCategory, issueName, friority, lineNumber, fullFileName;
		
		public <T> T get(JSONMap issue, Class<T> returnType) {
			return issue.get(name(), returnType);
		}
	}
	
	public static final String[] FORTIFY_FIELD_NAMES = Arrays.stream(FORTIFY_FIELDS.values()).map(Enum::name).toArray(String[]::new);
	
	public static abstract class AbstractFortifyIssuesProcessor<SP extends AbstractFortifyIssueSensorProperties> {
		private final SensorContext context;
		private final IFortifyScannerSideConnectionHelper connHelper;
		private final SP sensorProperties;
		private final List<InputFile> inputFiles;
		private boolean updateIssueSearchOptions = true;
		
		public AbstractFortifyIssuesProcessor(SensorContext context, IFortifyScannerSideConnectionHelper connHelper, SP sensorProperties) {
			this.connHelper = connHelper;
			this.context = context;
			this.sensorProperties = sensorProperties;
			this.inputFiles = getInputFilesSortedByPathLength(context.fileSystem());
		}

		public final void processIssues() {
			IFortifyScannerSideConnectionHelper connHelper = getConnHelper();
			String externalListId = FortifyRulesDefinition.getExternalListId();
			if ( externalListId==null ) {
				ActiveRule activeRule = context.activeRules().find(RuleKey.of(FortifyRulesDefinition.REPOSITORY_KEY, FortifyRulesDefinition.RULE_KEY_OTHER));
				if ( activeRule!=null ) {
					processAllIssues(activeRule);
				}
			} else {
				Set<String> availableGroupIds = getAvailableGroupIds(connHelper, externalListId);
				for ( ActiveRule activeRule : context.activeRules().findByRepository(FortifyRulesDefinition.REPOSITORY_KEY) ) {
					String externalCategory = activeRule.internalKey();
					if ( availableGroupIds.contains(externalCategory) ) {
						processIssuesForExternalCategory(activeRule, externalListId, externalCategory);
					}
				}
			}
		}

		private HashSet<String> getAvailableGroupIds(IFortifyScannerSideConnectionHelper connHelper, String externalListId) {
			return new HashSet<>(getIssueGroupsBaseQuery().build().getAll().getValues("id", String.class));
		}
		
		// We sort by path name length, such that shorter paths will be matched first
		public static final List<InputFile> getInputFilesSortedByPathLength(FileSystem fs) {
			// This uses deprecated SQ API, but there seems to be no non-deprecated methods for getting
			// the full file name; uri() is not deprecated but not guaranteed to return the actual file
			// location.
			List<InputFile> result = StreamSupport.stream(fs.inputFiles(fs.predicates().all()).spliterator(), false).collect(Collectors.toList());
			result.sort(Comparator.<InputFile>comparingInt(inputFile -> inputFile.path().toString().length()));
	        return result;
		}
		
		protected final SSCApplicationVersionIssueGroupsQueryBuilder getIssueGroupsBaseQuery() {
			SSCApplicationVersionIssueGroupsQueryBuilder result = connHelper.getConnection().api(SSCIssueGroupsAPI.class)
				.queryIssueGroups(connHelper.getApplicationVersionId())
				.paramFilterSet(sensorProperties.getFilterSetId(context))
				.paramFields("id");
			updateIssueSearchOptions = false; // We assume that SSC's issue search options are not changed in-between requests
			return result;
		}
		
		protected final SSCApplicationVersionIssuesQueryBuilder getIssuesBaseQuery() {
			SSCApplicationVersionIssuesQueryBuilder result = connHelper.getConnection().api(SSCIssueAPI.class).queryIssues(connHelper.getApplicationVersionId())
				.paramFilterSet(sensorProperties.getFilterSetId(context))
				.paramFields(FORTIFY_FIELD_NAMES)
				.includeHidden(false)
				.includeRemoved(false)
				.includeSuppressed(false)
				.updateIssueSearchOptions(updateIssueSearchOptions)
				.paramQm(QueryMode.issues);
			updateIssueSearchOptions = false; // We assume that SSC's issue search options are not changed in-between requests
			return result;
		}
		
		protected final SSCApplicationVersionIssuesQueryBuilder getIssuesBaseQuery(String externalListId, String externalCategory) {
			return getIssuesBaseQuery()
				.paramGroupingType(externalListId).paramGroupId(externalCategory);
		}
		
		public abstract void processAllIssues(ActiveRule activeRule);
		public abstract void processIssuesForExternalCategory(ActiveRule activeRule, String externalListId, String externalCategory);
		
		public SensorContext getContext() {
			return context;
		}
		
		public IFortifyScannerSideConnectionHelper getConnHelper() {
			return connHelper;
		}
		
		public SP getSensorProperties() {
			return sensorProperties;
		}
		
		public List<InputFile> getInputFiles() {
			return inputFiles;
		}
	}
	
	
	public static abstract class AbstractFortifyIssueProcessor extends AbstractJSONMapProcessor {
		private static final Logger LOG = Loggers.get(AbstractFortifyIssueProcessor.class);
		private final SensorContext context;
		private final List<InputFile> inputFiles;
		private final ActiveRule activeRule;
		
		public AbstractFortifyIssueProcessor(SensorContext context, ActiveRule activeRule, List<InputFile> inputFiles) {
			this.context = context;
			this.inputFiles = inputFiles;
			this.activeRule = activeRule;
		}

		@Override
		public final void process(JSONMap issue) {
			if ( !ignoreIssue(issue) ) {
				try {
					// Get the inputFile for the current issue
					InputFile inputFile = getInputFileForIssue(inputFiles, issue);
					if ( inputFile==null ) {
						LOG.debug("No InputFile found for "+FORTIFY_FIELDS.fullFileName.get(issue, String.class)+" (vulnerability id "+FORTIFY_FIELDS.id.get(issue, String.class)+")");
						createIssueWithoutInputFile(issue);
					} else {
						LOG.debug("InputFile found for "+FORTIFY_FIELDS.fullFileName.get(issue, String.class)+" (vulnerability id "+FORTIFY_FIELDS.id.get(issue, String.class)+")");
						createIssueOnInputFile(inputFile, issue);
					}
				} catch ( RuntimeException e ) {
					LOG.error("Error creating SonarQube issue for vulnerability id "+FORTIFY_FIELDS.id.get(issue, String.class), e);
				}
			}
		}
		
		protected boolean ignoreIssue(JSONMap issue) {
			return false;
		}
		
		protected void createIssueWithoutInputFile(JSONMap issue) {}
		
		protected void createIssueOnInputFile(InputFile inputFile, JSONMap issue) {
			NewIssue newIssue = createNewIssue(issue);
			addIssueLocation(newIssue, inputFile, issue);
			// TODO Low: Add .addFlow(Fortify evidence flow)
			newIssue.save();
		}

		protected NewIssue createNewIssue(JSONMap issue) {
			String friority = StringUtils.lowerCase(FORTIFY_FIELDS.friority.get(issue, String.class));
			NewIssue newIssue = getContext().newIssue().forRule(getActiveRule().ruleKey());
			newIssue.overrideSeverity(FortifyConstants.FRIORITY_TO_SEVERITY(friority));
			return newIssue;
		}

		protected void addIssueLocation(NewIssue newIssue, InputFile inputFile, JSONMap issue) {
			int lineNumber = Math.max(1, FORTIFY_FIELDS.lineNumber.get(issue, Integer.class));
			NewIssueLocation primaryLocation = newIssue.newLocation()
					.on(inputFile)
					.at(inputFile.selectLine(lineNumber))
					.message(getIssueMessage(issue, false));
			newIssue.at(primaryLocation);
		}

		protected String getIssueMessage(JSONMap issue, boolean includeFilename) {
			String prefix = !includeFilename ? "" : (FORTIFY_FIELDS.fullFileName.get(issue, String.class)+" - ");
			return prefix 
				+ FORTIFY_FIELDS.issueName.get(issue, String.class) 
				+ " ("+FORTIFY_FIELDS.deepLink.get(issue, String.class)+")";
		}
		
		protected final InputFile getInputFileForIssue(List<InputFile> inputFiles, JSONMap issue) {
			Path fullFileName = Paths.get(FORTIFY_FIELDS.fullFileName.get(issue, String.class));
			for ( InputFile inputFile : inputFiles ) {
				Path path = inputFile.path();
				if ( path.endsWith(fullFileName) ) {
					return inputFile;
				}
			}
			return null;
		}

		public final SensorContext getContext() {
			return context;
		}

		public final List<InputFile> getInputFiles() {
			return inputFiles;
		}

		public final ActiveRule getActiveRule() {
			return activeRule;
		}
	}
}
