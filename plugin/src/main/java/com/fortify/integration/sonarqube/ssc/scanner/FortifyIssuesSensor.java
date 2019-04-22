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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.PropertyType;
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
import com.fortify.client.ssc.api.SSCIssueTemplateAPI;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionIssuesQueryBuilder.QueryMode;
import com.fortify.integration.sonarqube.ssc.FortifyConstants;
import com.fortify.integration.sonarqube.ssc.rule.FortifyRulesDefinition;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.processor.AbstractJSONMapProcessor;
import com.fortify.util.rest.json.processor.IJSONMapProcessor;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This SonarQube {@link Sensor} implementation retrieves vulnerability data from SSC and
 * reports these vulnerabilities as SonarQube violations.
 * 
 * @author Ruud Senden
 *
 */

/*
 * TODO
 * Add list of issues for which the corresponding language rule has not be activated, to be displayed on dashboard
 * Add list of issues for which the match expression is false
 * Retrieve list of vulnerabilities only once
 * Retrieve only relevant issue fields from SSC
 * Use SSC search string (see FortifyBugTrackerUtility) instead of SpEL to filter issues to be reported?
 * Configure SSC search string in plugin configuration instead of rule configuration?
 * Report all issues on generic Fortify rule? Disables language-based filtering in SQ UI, but makes configuration of plugin easier (no need to activate rules) 
 */
public class FortifyIssuesSensor implements Sensor {
	private static final Logger LOG = Loggers.get(FortifyIssuesSensor.class);
	private static final String PRP_ENABLE_ISSUES = "sonar.fortify.issues.enable";
	private static final String PRP_FILTER_SET = "sonar.fortify.ssc.filterset";
	
	private final FortifySSCScannerSideConnectionHelper connHelper;
	private final FortifyIssueHelper issueHelper; // TODO Use this
	
	/**
	 * Constructor for injecting dependencies
	 * @param connFactory
	 */
	public FortifyIssuesSensor(FortifySSCScannerSideConnectionHelper connFactory, FortifyIssueHelper issueHelper) {
		this.connHelper = connFactory;
		this.issueHelper = issueHelper;
	}
	
	@Override
	public void describe(SensorDescriptor descriptor) {
		descriptor.name("Fortify issue collection");
	}
	
	/**
	 * Sensor implementation that retrieves issue details from SSC, and reports them as SonarQube violations
	 */
	@Override
	public void execute(SensorContext context) {
		final String defaultMatchExpression = "(suppressed==false && hidden==false && engineCategory=='STATIC')";
		if ( isActive(context) ) {
			JSONMap filterSet = getSonarQubeFilterSet(context);
			FileSystem fs = context.fileSystem();
			processFortifyIssues(connHelper, filterSet, new AbstractJSONMapProcessor() {	
				@Override
				public void process(JSONMap issue) {
					try {
						// Get the inputFile for the current issue
						InputFile inputFile = getInputFile(fs, issue);
						if ( inputFile != null ) { // Skip issue if filename doesn't belong to current module
							ActiveRule rule = getActiveRule(context, inputFile, issue);
							if ( rule != null ) {
								if ( SpringExpressionUtil.evaluateExpression(issue, defaultMatchExpression, Boolean.class)) {
									createIssue(context, rule, inputFile, issue);
								}
							}
						}
					} catch ( RuntimeException e ) {
						LOG.error("Error creating SonarQube issue for vulnerability id "+issue.get("id", String.class), e);
					}
				}
				
				private void createIssue(SensorContext context, ActiveRule rule, InputFile inputFile, JSONMap issue) {
					String friority = StringUtils.lowerCase(issue.get("friority", String.class));
					NewIssue newIssue = context.newIssue().forRule(rule.ruleKey());
					addIssueLocation(context, newIssue, inputFile, issue);
					newIssue.overrideSeverity(FortifyConstants.FRIORITY_TO_SEVERITY(friority));
					// TODO Low: Add .addFlow(Fortify evidence flow)
					newIssue.save();
				}

				private void addIssueLocation(SensorContext context, NewIssue newIssue, InputFile inputFile, JSONMap issue) {
					int lineNumber = issue.get("lineNumber", Integer.class);
					NewIssueLocation primaryLocation = newIssue.newLocation()
							.on(inputFile)
							.at(inputFile.selectLine(lineNumber))
							.message(getIssueMessage(context, issue));
					newIssue.at(primaryLocation);
				}

				private ActiveRule getActiveRule(SensorContext context, InputFile inputFile, JSONMap issue) {
					// TODO Map issue category to SonarQube rule 
					return context.activeRules().find(RuleKey.of(FortifyRulesDefinition.REPOSITORY_KEY, FortifyRulesDefinition.RULE_KEY_OTHER));
				}

				protected String getIssueMessage(SensorContext sensorContext, JSONMap issue) {
					return issue.get("issueName", String.class)+" ("+issue.get("deepLink", String.class)+")";
				}
				
				protected final InputFile getInputFile(FileSystem fs, JSONMap issue) {
					Path fullFileName = Paths.get(issue.get("fullFileName", String.class));
					Iterable<InputFile> files = fs.inputFiles(fs.predicates().all());
					for ( InputFile inputFile : files ) {
						Path path = Paths.get(inputFile.uri());
						if ( path.endsWith(fullFileName) ) {
							return inputFile;
						}
					}
					return null;
				}
			});
		}
	}
	
	/**
	 * Get the filter set specified through the {@link FortifyConstants#PRP_FILTER_SET} setting
	 * @param context sensor context
	 * @param conn SSC connection
	 * @return JSONObject representing the specified filter set, or the default filter set if not specified
	 * @throws IllegalArgumentException if specified filter set cannot be found
	 */
	
	private JSONMap getSonarQubeFilterSet(SensorContext context) {
		JSONMap filterSet = null;
		JSONList filterSets = connHelper.getConnection().api(SSCIssueTemplateAPI.class).queryApplicationVersionFilterSets(connHelper.getApplicationVersionId()).build().getAll();
		String filterSetGuidOrTitle = context.config().get(PRP_FILTER_SET).orElse(null);
		if ( StringUtils.isNotBlank(filterSetGuidOrTitle) ) {
			String matchExpr = MessageFormat.format("guid==''{0}'' || title==''{0}''", new Object[]{filterSetGuidOrTitle});
			filterSet = filterSets.find(matchExpr, true, JSONMap.class);
			if ( filterSet==null ) {
				throw new IllegalArgumentException("Unknown filter set "+filterSetGuidOrTitle);
			}
		}
		return filterSet==null ? getSSCDefaultFilterSet(filterSets) : filterSet;
	}
	
	private JSONMap getSSCDefaultFilterSet(JSONList filterSets) {
		return filterSets.find("defaultFilterSet", true, JSONMap.class);
	}

	/**
	 * Retrieve vulnerability data from SSC, and process each vulnerability using the given {@link IFortifyIssueProcessor}
	 * @param conn
	 * @param processor
	 */
	protected void processFortifyIssues(FortifySSCScannerSideConnectionHelper connFactory, JSONMap filterSet, IJSONMapProcessor processor) {
		connFactory.getConnection().api(SSCIssueAPI.class).queryIssues(connFactory.getApplicationVersionId())
			.paramFilterSet(filterSet.get("guid",String.class))
			.includeHidden(false)
			.includeRemoved(false)
			.includeSuppressed(false)
			.paramFilter("ISSUE[11111111-1111-1111-1111-111111111151]:SCA")
			.paramQm(QueryMode.issues).build().processAll(processor);
	}

	/**
	 * @param context
	 * @return true if SSC connection is available and issue collection is enabled, false otherwise
	 */
	private final boolean isActive(SensorContext context) {
		// Check whether connection is available
		boolean result = connHelper.isConnectionAvailable();
		// Check whether issue collection is enabled
		result &= context.config().getBoolean(PRP_ENABLE_ISSUES).orElse(true);
		// TODO Check whether there are any active Fortify rules
		return result;
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
}