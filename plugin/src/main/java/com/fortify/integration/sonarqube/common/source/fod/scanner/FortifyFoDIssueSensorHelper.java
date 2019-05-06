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
package com.fortify.integration.sonarqube.common.source.fod.scanner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

import com.fortify.client.fod.api.FoDVulnerabilityAPI;
import com.fortify.client.fod.api.FoDVulnerabilityFiltersAPI;
import com.fortify.client.fod.api.query.builder.FoDReleaseVulnerabilitiesQueryBuilder;
import com.fortify.client.fod.api.query.builder.FoDReleaseVulnerabilityFiltersQueryBuilder;
import com.fortify.integration.sonarqube.common.FortifyConstants;
import com.fortify.integration.sonarqube.common.rule.FortifyRulesDefinition;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.processor.AbstractJSONMapProcessor;

/**
 * This helper class provides functionality for loading Fortify issues from
 * FoD, and reporting those issues on SonarQube source files.
 * 
 * TODO Add more JavaDoc
 * TODO Remove code duplication between FoD and SSC implementations
 *  
 * @author Ruud Senden
 *
 */
public class FortifyFoDIssueSensorHelper {
	public static enum FOD_ISSUE_FIELDS {
		id, deepLink, scantype, category, severityString, lineNumber, primaryLocationFull;
		
		public <T> T get(JSONMap issue, Class<T> returnType) {
			return issue.get(name(), returnType);
		}
	}
	
	private static final Map<String,String> externalListNameToFieldNameMap = getExternalListNameToFieldNameMap(); 
	
	public static final String[] FOD_ISSUE_FIELD_NAMES = Arrays.stream(FOD_ISSUE_FIELDS.values()).map(Enum::name).toArray(String[]::new);
	
	public static abstract class AbstractFortifyIssuesProcessor<SP extends AbstractFortifyFoDIssueSensorProperties> {
		private final SensorContext context;
		private final IFortifyFoDScannerSideConnectionHelper connHelper;
		private final SP sensorProperties;
		private final List<InputFile> inputFiles;
		
		public AbstractFortifyIssuesProcessor(SensorContext context, IFortifyFoDScannerSideConnectionHelper connHelper, SP sensorProperties) {
			this.connHelper = connHelper;
			this.context = context;
			this.sensorProperties = sensorProperties;
			this.inputFiles = getInputFilesSortedByPathLength(context.fileSystem());
		}

		public final void processIssues() {
			IFortifyFoDScannerSideConnectionHelper connHelper = getConnHelper();
			String externalListName = FortifyRulesDefinition.getExternalListName();
			if ( externalListName==null ) {
				ActiveRule activeRule = context.activeRules().find(RuleKey.of(FortifyRulesDefinition.REPOSITORY_KEY, FortifyRulesDefinition.RULE_KEY_OTHER));
				if ( activeRule!=null ) {
					processAllIssues(activeRule);
				}
			} else {
				Set<String> availableGroupIds = getAvailableGroupIds(connHelper, externalListName);
				for ( ActiveRule activeRule : context.activeRules().findByRepository(FortifyRulesDefinition.REPOSITORY_KEY) ) {
					String externalCategory = activeRule.internalKey();
					if ( availableGroupIds.contains(externalCategory) ) {
						processIssuesForExternalCategory(activeRule, externalListName, externalCategory);
					}
				}
			}
		}

		private HashSet<String> getAvailableGroupIds(IFortifyFoDScannerSideConnectionHelper connHelper, String externalListName) {
			return new HashSet<>(getIssueGroupsBaseQuery().paramFieldName(getFieldName(externalListName)).build().getAll().getValues("id", String.class));
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
		
		protected final FoDReleaseVulnerabilityFiltersQueryBuilder getIssueGroupsBaseQuery() {
			FoDReleaseVulnerabilityFiltersQueryBuilder result = connHelper.getConnection().api(FoDVulnerabilityFiltersAPI.class)
				.queryVulnerabilityFilters(connHelper.getReleaseId())
				.paramFields("id");
			return result;
		}
		
		protected final FoDReleaseVulnerabilitiesQueryBuilder getIssuesBaseQuery() {
			FoDReleaseVulnerabilitiesQueryBuilder result = connHelper.getConnection().api(FoDVulnerabilityAPI.class)
				.queryVulnerabilities(connHelper.getReleaseId())
				.paramFields(FOD_ISSUE_FIELD_NAMES)
				.paramIncludeFixed(false)
				.paramIncludeSuppressed(false);
			return result;
		}
		
		protected final FoDReleaseVulnerabilitiesQueryBuilder getIssuesBaseQuery(String externalListName, String externalCategory) {
			return getIssuesBaseQuery()
				.paramFilterAnd(getFieldName(externalListName), externalCategory);
		}
		
		private String getFieldName(String externalListName) {
			return externalListNameToFieldNameMap.get(externalListName);
		}

		public abstract void processAllIssues(ActiveRule activeRule);
		public abstract void processIssuesForExternalCategory(ActiveRule activeRule, String externalListId, String externalCategory);
		
		public SensorContext getContext() {
			return context;
		}
		
		public IFortifyFoDScannerSideConnectionHelper getConnHelper() {
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
						LOG.debug("No InputFile found for "+FOD_ISSUE_FIELDS.primaryLocationFull.get(issue, String.class)+" (vulnerability id "+FOD_ISSUE_FIELDS.id.get(issue, String.class)+")");
						createIssueWithoutInputFile(issue);
					} else {
						LOG.debug("InputFile found for "+FOD_ISSUE_FIELDS.primaryLocationFull.get(issue, String.class)+" (vulnerability id "+FOD_ISSUE_FIELDS.id.get(issue, String.class)+")");
						createIssueOnInputFile(inputFile, issue);
					}
				} catch ( RuntimeException e ) {
					LOG.error("Error creating SonarQube issue for vulnerability id "+FOD_ISSUE_FIELDS.id.get(issue, String.class), e);
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
			String friority = StringUtils.lowerCase(FOD_ISSUE_FIELDS.severityString.get(issue, String.class));
			NewIssue newIssue = getContext().newIssue().forRule(getActiveRule().ruleKey());
			newIssue.overrideSeverity(FortifyConstants.FRIORITY_TO_SEVERITY(friority));
			return newIssue;
		}

		protected void addIssueLocation(NewIssue newIssue, InputFile inputFile, JSONMap issue) {
			int lineNumber = Math.max(1, FOD_ISSUE_FIELDS.lineNumber.get(issue, Integer.class));
			NewIssueLocation primaryLocation = newIssue.newLocation()
					.on(inputFile)
					.at(inputFile.selectLine(lineNumber))
					.message(getIssueMessage(issue, false));
			newIssue.at(primaryLocation);
		}

		protected String getIssueMessage(JSONMap issue, boolean includeFilename) {
			String prefix = !includeFilename ? "" : (FOD_ISSUE_FIELDS.primaryLocationFull.get(issue, String.class)+" - ");
			return prefix 
				+ FOD_ISSUE_FIELDS.category.get(issue, String.class) 
				+ " ("+FOD_ISSUE_FIELDS.deepLink.get(issue, String.class)+")";
		}
		
		protected final InputFile getInputFileForIssue(List<InputFile> inputFiles, JSONMap issue) {
			Path fullFileName = Paths.get(FOD_ISSUE_FIELDS.primaryLocationFull.get(issue, String.class));
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


	private static final Map<String, String> getExternalListNameToFieldNameMap() {
		Map<String, String> result = new HashMap<>();
		result.put("NIST SP 800-53 Rev.4", "");
        result.put("CWE", "");
        result.put("OWASP Top 10 2004", "owasp2004");
        result.put("OWASP Top 10 2007", "owasp2007");
        result.put("OWASP Top 10 2010", "owasp2010");
        result.put("OWASP Top 10 2013", "owasp2013");
        result.put("OWASP Top 10 2017", "owasp2017");
        result.put("OWASP Mobile 2014", "owasp2014MobileTop10");
        result.put("SANS Top 25 2009", "sans2009");
        result.put("FISMA", "fisma");
        result.put("PCI 1.1", "");
        result.put("PCI 1.2", "");
        result.put("PCI 2.0", "pci2");
        result.put("PCI 3.0", "pci3");
        result.put("PCI 3.1", "pci3_1");
        result.put("PCI 3.2", "pci3_2");
        result.put("PCI 3.2.1", "");
        result.put("STIG 3.1", "");
        result.put("STIG 3.4", "");
        result.put("STIG 3.5", "");
        result.put("STIG 3.6", "");
        result.put("STIG 3.7", "");
        result.put("STIG 3.9", "sti3_9");
        result.put("STIG 3.10", "");
        result.put("STIG 4.1", "sti4_1");
        result.put("STIG 4.2", "");
        result.put("STIG 4.3", "sti4_3");
        result.put("STIG 4.4", "");
        result.put("STIG 4.5", "");
        result.put("STIG 4.6", "");
        result.put("STIG 4.7", "");
        result.put("STIG 4.8", "");
        result.put("STIG 4.9", "");
        result.put("DISA CCI 2", "");
        result.put("WASC 24 + 2", "wasc24_2");
        result.put("WASC 2.00", "");
        result.put("SANS Top 25 2010", "");
        result.put("SANS Top 25 2011", "");
        result.put("MISRA C 2012", "");
        result.put("MISRA C++ 2008", "");
        result.put("GDPR", "");
		return result;
	}
}
