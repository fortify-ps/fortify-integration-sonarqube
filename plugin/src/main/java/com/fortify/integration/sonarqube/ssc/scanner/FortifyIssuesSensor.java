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
package com.fortify.integration.sonarqube.ssc.scanner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.client.ssc.api.SSCIssueAPI;
import com.fortify.client.ssc.api.SSCIssueGroupsAPI;
import com.fortify.client.ssc.api.SSCIssueTemplateAPI;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionIssuesQueryBuilder;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionIssuesQueryBuilder.QueryMode;
import com.fortify.integration.sonarqube.ssc.FortifyConstants;
import com.fortify.integration.sonarqube.ssc.rule.FortifyRulesDefinition;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter.MatchMode;
import com.fortify.util.rest.json.preprocessor.filter.JSONMapFilterSpEL;
import com.fortify.util.rest.json.processor.AbstractJSONMapProcessor;

/**
 * This SonarQube {@link Sensor} implementation retrieves vulnerability data from SSC and
 * reports these vulnerabilities as SonarQube violations.
 * 
 * @author Ruud Senden
 *
 */

/*
 * TODO
 * Retrieve only relevant issue fields from SSC
 * Use SSC search string (see FortifyBugTrackerUtility) instead of SpEL to filter issues to be reported?
 * Configure SSC search string in plugin configuration instead of rule configuration?
 * Report all issues on generic Fortify rule? Disables language-based filtering in SQ UI, but makes configuration of plugin easier (no need to activate rules) 
 */
@ScannerSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class FortifyIssuesSensor extends FortifyAbstractProjectSensor {
	//private static final Logger LOG = Loggers.get(FortifyIssuesSensor.class);
	private static final String PRP_ENABLE_ISSUES = "sonar.fortify.issues.enable";
	private static final String PRP_FILTER_SET = "sonar.fortify.ssc.filterset";
	
	private static enum fortifyFields {
		deepLink, engineCategory, issueName, friority, lineNumber, fullFileName;
		
		public <T> T get(JSONMap issue, Class<T> returnType) {
			return issue.get(name(), returnType);
		}
	}
	
	private static final String[] fortifyFieldNames = Arrays.stream(fortifyFields.values()).map(Enum::name).toArray(String[]::new);
	
	/**
	 * Constructor for injecting dependencies
	 * @param connFactory
	 */
	public FortifyIssuesSensor(FortifySSCScannerSideConnectionHelper connHelper) {
		super(connHelper);
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
		FortifySSCScannerSideConnectionHelper connHelper = getConnHelper();
		List<InputFile> inputFiles = getInputFilesSortedByPathLength(context.fileSystem());
		String externalListId = FortifyRulesDefinition.getExternalListId();
		boolean updateIssueSearchOptions = true;
		if ( externalListId==null ) {
			ActiveRule activeRule = context.activeRules().find(RuleKey.of(FortifyRulesDefinition.REPOSITORY_KEY, FortifyRulesDefinition.RULE_KEY_OTHER));
			if ( activeRule!=null ) {
				getIssuesBaseQuery(context, updateIssueSearchOptions).build().processAll(new FortifyIssueProcessor(context, activeRule, inputFiles));
			}
		} else {
			Set<String> availableGroupIds = new HashSet<>(connHelper.getConnection().api(SSCIssueGroupsAPI.class).queryIssueGroups(connHelper.getApplicationVersionId()).paramGroupingType(externalListId).build().getAll().getValues("id", String.class));
			for ( ActiveRule activeRule : context.activeRules().findByRepository(FortifyRulesDefinition.REPOSITORY_KEY) ) {
				String externalCategory = activeRule.internalKey();
				if ( availableGroupIds.contains(externalCategory) ) {
					getIssuesBaseQuery(context, updateIssueSearchOptions).paramGroupingType(externalListId).paramGroupId(externalCategory)
						.build().processAll(new FortifyIssueProcessor(context, activeRule, inputFiles));
					updateIssueSearchOptions = false;
				}
			}
		}
		
		
		
	}

	private SSCApplicationVersionIssuesQueryBuilder getIssuesBaseQuery(SensorContext context, boolean updateIssueSearchOptions) {
		FortifySSCScannerSideConnectionHelper connHelper = getConnHelper();
		return connHelper.getConnection().api(SSCIssueAPI.class).queryIssues(connHelper.getApplicationVersionId())
			.paramFilterSet(getFilterSetId(context))
			.paramFields(fortifyFieldNames)
			.includeHidden(false)
			.includeRemoved(false)
			.includeSuppressed(false)
			.updateIssueSearchOptions(updateIssueSearchOptions)
			.paramFilter("ISSUE[11111111-1111-1111-1111-111111111151]:SCA")
			.preProcessor(new JSONMapFilterSpEL(MatchMode.INCLUDE, fortifyFields.engineCategory+"=='STATIC'"))
			.paramQm(QueryMode.issues);
	}
	
	/**
	 * Get the filter set specified through the {@link FortifyConstants#PRP_FILTER_SET} setting
	 * @param context sensor context
	 * @param conn SSC connection
	 * @return JSONObject representing the specified filter set, or the default filter set if not specified
	 * @throws IllegalArgumentException if specified filter set cannot be found
	 */
	
	private String getFilterSetId(SensorContext context) {
		JSONMap filterSet = null;
		JSONList filterSets = getConnHelper().getConnection().api(SSCIssueTemplateAPI.class).queryApplicationVersionFilterSets(getConnHelper().getApplicationVersionId()).build().getAll();
		String filterSetGuidOrTitle = context.config().get(PRP_FILTER_SET).orElse(null);
		if ( StringUtils.isNotBlank(filterSetGuidOrTitle) ) {
			String matchExpr = MessageFormat.format("guid==''{0}'' || title==''{0}''", new Object[]{filterSetGuidOrTitle});
			filterSet = filterSets.find(matchExpr, true, JSONMap.class);
			if ( filterSet==null ) {
				throw new IllegalArgumentException("Unknown filter set "+filterSetGuidOrTitle);
			}
		}
		return (filterSet==null ? getSSCDefaultFilterSet(filterSets) : filterSet).get("id", String.class);
	}
	
	private JSONMap getSSCDefaultFilterSet(JSONList filterSets) {
		return filterSets.find("defaultFilterSet", true, JSONMap.class);
	}

	// We sort by path name length, such that shorter paths will be matched first
	private List<InputFile> getInputFilesSortedByPathLength(FileSystem fs) {
		List<InputFile> result = StreamSupport.stream(fs.inputFiles(fs.predicates().all()).spliterator(), false).collect(Collectors.toList());
		result.sort(Comparator.<InputFile>comparingInt(inputFile -> inputFile.path().toString().length()));
        return result;
	}
	

	/**
	 * @param context
	 * @return true if SSC connection is available and issue collection is enabled, false otherwise
	 */
	@Override
	protected final boolean isActive(SensorContext context) {
		return context.config().getBoolean(PRP_ENABLE_ISSUES).orElse(true);
	}

	public static final void addPropertyDefinitions(List<PropertyDefinition> propertyDefinitions) {
		propertyDefinitions.add(PropertyDefinition.builder(PRP_ENABLE_ISSUES)
				.name("Enable issues collection")
				.description("Enable collecting Fortify issues")
				.type(PropertyType.BOOLEAN)
				.defaultValue("true")
				.build());
		propertyDefinitions.add(PropertyDefinition.builder(PRP_FILTER_SET)
				.name("Filter set name/id")
				.description("Filter set name or id used to retrieve issue data from SSC (optional)")
				.type(PropertyType.STRING)
				.onQualifiers(Qualifiers.PROJECT)
				.build());
	}
	
	private static final class FortifyIssueProcessor extends AbstractJSONMapProcessor {
		private static final Logger LOG = Loggers.get(FortifyIssueProcessor.class);
		private final SensorContext context;
		private final List<InputFile> inputFiles;
		private final ActiveRule activeRule;
		
		public FortifyIssueProcessor(SensorContext context, ActiveRule activeRule, List<InputFile> inputFiles) {
			this.context = context;
			this.inputFiles = inputFiles;
			this.activeRule = activeRule;
		}

		@Override
		public void process(JSONMap issue) {
			try {
				// Get the inputFile for the current issue
				InputFile inputFile = getInputFileForIssue(issue);
				if ( inputFile != null ) { // Skip issue if filename doesn't belong to current module
					createIssue(inputFile, issue);
				}
			} catch ( RuntimeException e ) {
				LOG.error("Error creating SonarQube issue for vulnerability id "+issue.get("id", String.class), e);
			}
		}
		
		private void createIssue(InputFile inputFile, JSONMap issue) {
			String friority = StringUtils.lowerCase(fortifyFields.friority.get(issue, String.class));
			NewIssue newIssue = context.newIssue().forRule(activeRule.ruleKey());
			addIssueLocation(newIssue, inputFile, issue);
			newIssue.overrideSeverity(FortifyConstants.FRIORITY_TO_SEVERITY(friority));
			// TODO Low: Add .addFlow(Fortify evidence flow)
			newIssue.save();
		}

		private void addIssueLocation(NewIssue newIssue, InputFile inputFile, JSONMap issue) {
			int lineNumber = fortifyFields.lineNumber.get(issue, Integer.class);
			NewIssueLocation primaryLocation = newIssue.newLocation()
					.on(inputFile)
					.at(inputFile.selectLine(lineNumber))
					.message(getIssueMessage(issue));
			newIssue.at(primaryLocation);
		}

		

		private String getIssueMessage(JSONMap issue) {
			return fortifyFields.issueName.get(issue, String.class)+" ("+fortifyFields.deepLink.get(issue, String.class)+")";
		}
		
		private final InputFile getInputFileForIssue(JSONMap issue) {
			Path fullFileName = Paths.get(fortifyFields.fullFileName.get(issue, String.class));
			for ( InputFile inputFile : inputFiles ) {
				Path path = inputFile.path();
				if ( path.endsWith(fullFileName) ) {
					return inputFile;
				}
			}
			return null;
		}
	}
}