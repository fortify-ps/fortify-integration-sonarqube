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
package com.fortify.plugin.sonarqube.batch;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.measures.Metric;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.plugin.sonarqube.FortifyConstants;
import com.fortify.plugin.sonarqube.FortifySSCConnectionFactory;
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
public class FortifyIssueMetricsAndSensor extends AbstractFortifyMetricsAndSensor implements Sensor {
	private static final Logger LOG = Loggers.get(FortifyIssueMetricsAndSensor.class); 
	private final Map<String, Integer> issueCounts = new HashMap<>();
	private final Map<String, String> effectiveLanguageFilters = new LinkedHashMap<>();
	private final Set<String> processedIssues = new HashSet<String>();
	
	/**
	 * Constructor for injecting {@link FortifySSCConnectionFactory} instance
	 * @param connFactory
	 */
	public FortifyIssueMetricsAndSensor(FortifySSCConnectionFactory connFactory) {
		super(connFactory);
	}

	/**
	 * Add metrics for Fortify issue counts as reported in SonarQube (based on filter set 
	 * configuration and rule filters), filter and filter set meta data, and unresolved
	 * files messages.
	 */
	@Override
	protected void addMetrics(Map<Metric<Serializable>, MetricValueRetriever> metricsMap) {
		metricsMap.put(new Metric.Builder("fortify.sonarqube.CFPO", "Critical Priority Issues (SonarQube)",
				Metric.ValueType.INT).setDescription("Critical Issues (SonarQube)").setDirection(Metric.DIRECTION_WORST)
				.setQualitative(true).setDomain("Fortify").create(), new IssueCountMetricValueRetriever("critical"));
		
		metricsMap.put(new Metric.Builder("fortify.sonarqube.HFPO", "High Priority Issues (SonarQube)",
				Metric.ValueType.INT).setDescription("High Issues (SonarQube)").setDirection(Metric.DIRECTION_WORST)
				.setQualitative(true).setDomain("Fortify").create(), new IssueCountMetricValueRetriever("high"));
		
		metricsMap.put(new Metric.Builder("fortify.sonarqube.MFPO", "Medium Priority Issues (SonarQube)",
				Metric.ValueType.INT).setDescription("Medium Issues (SonarQube)").setDirection(Metric.DIRECTION_WORST)
				.setQualitative(true).setDomain("Fortify").create(), new IssueCountMetricValueRetriever("medium"));
		
		metricsMap.put(new Metric.Builder("fortify.sonarqube.LFPO", "Low Priority Issues (SonarQube)",
				Metric.ValueType.INT).setDescription("Low Issues (SonarQube)").setDirection(Metric.DIRECTION_WORST)
				.setQualitative(true).setDomain("Fortify").create(), new IssueCountMetricValueRetriever("low"));
		
		metricsMap.put(new Metric.Builder("fortify.json.filterSets", "Relevant Filter Sets",
				Metric.ValueType.DATA).setDescription("Relevant Filter Sets").setDirection(Metric.DIRECTION_NONE)
				.setQualitative(false).setDomain("Fortify").setHidden(true).create(), 
				new AbstractMetricValueRetriever() {
					@Override
					public Serializable getMetricValue(SensorContext context, Metric<Serializable> metric, FortifySSCConnectionFactory connFactory) {
						JSONMap result = new JSONMap();
						result.put("sonarqube", getSonarQubeFilterSet(context, connFactory));
						result.put("ssc", getSSCDefaultFilterSet(connFactory));
						return result.toString();
					}
				});
		
		metricsMap.put(new Metric.Builder("fortify.json.effectiveLanguageFilters", "Effective Language Filters",
				Metric.ValueType.DATA).setDescription("Effective Language Filters").setDirection(Metric.DIRECTION_NONE)
				.setQualitative(false).setDomain("Fortify").setHidden(true).create(), 
				new AbstractMetricValueRetriever() {
					@Override
					public Serializable getMetricValue(SensorContext context, Metric<Serializable> metric, FortifySSCConnectionFactory connFactory) {
						return new JSONMap(effectiveLanguageFilters).toString();
					}
				});
		
		metricsMap.put(new Metric.Builder("fortify.json.unresolvedIssues", "Unresolved Issues",
				Metric.ValueType.DATA).setDescription("Unresolved Issue").setDirection(Metric.DIRECTION_NONE)
				.setQualitative(false).setDomain("Fortify").setHidden(false).create(), 
				new AbstractMetricValueRetriever() {
					@Override
					public Serializable getMetricValue(SensorContext context, Metric<Serializable> metric, FortifySSCConnectionFactory connFactory) {
						final JSONList result = new JSONList();
						JSONMap filterSet = getSonarQubeFilterSet(context, connFactory);
						processFortifyIssues(connFactory, filterSet, new AbstractJSONMapProcessor() {
							@Override
							public void process(JSONMap issue) {
								String vulnId = issue.get("id", String.class);
								if ( !processedIssues.contains(vulnId) ) {
									JSONMap newIssue = new JSONMap();
									newIssue.put("path", issue.get("fullFileName", String.class));
									newIssue.put("lineNumber", issue.get("lineNumber", String.class));
									newIssue.put("issueName", issue.get("issueName", String.class));
									newIssue.put("deepLink", issue.get("deepLink", String.class));
									result.add(newIssue);
								}
							}
						});
						return result.toString();
					}
				});
	}
	
	/**
	 * Sensor implementation that retrieves issue details from SSC, and reports them as SonarQube violations
	 */
	@Override
	public void executeBeforeMetricsCalculation(SensorContext context) {
		final String defaultMatchExpression = "(suppressed==false && hidden==false && engineCategory=='STATIC')";
		if ( isActive(context) ) {
			final FortifySSCConnectionFactory connFactory = getConnFactory();
			JSONMap filterSet = getSonarQubeFilterSet(context, connFactory);
			FileSystem fs = context.fileSystem();
			processFortifyIssues(connFactory, filterSet, new AbstractJSONMapProcessor() {	
				@Override
				public void process(JSONMap issue) {
					String vulnId = issue.get("id", String.class);
					// Skip issue if it has already been processed (for another module) 
					if ( processedIssues.contains(vulnId) ) { return; }
					try {
						// Get the inputFile for the current issue
						InputFile inputFile = getInputFile(fs, issue);
						if ( inputFile != null ) { // Skip issue if filename doesn't belong to current module
							processedIssues.add(vulnId);
							ActiveRule rule = getActiveRule(context, inputFile);
							if ( rule != null ) {
								String matchExpression = rule.param(FortifyConstants.RULE_PARAM_FILTER_KEY).replace('\n', ' ');
								if ( StringUtils.isNotBlank(matchExpression) ) {
									effectiveLanguageFilters.put(rule.language(), matchExpression);
								}
								matchExpression = StringUtils.isBlank(matchExpression) 
										? defaultMatchExpression 
										: defaultMatchExpression+" && ("+matchExpression+")";
								if ( SpringExpressionUtil.evaluateExpression(issue, matchExpression, Boolean.class)) {
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
					addIssueCount(friority);
				}

				private void addIssueLocation(SensorContext context, NewIssue newIssue, InputFile inputFile, JSONMap issue) {
					int lineNumber = issue.get("lineNumber", Integer.class);
					NewIssueLocation primaryLocation = newIssue.newLocation()
							.on(inputFile)
							.at(inputFile.selectLine(lineNumber))
							.message(getIssueMessage(context, issue));
					newIssue.at(primaryLocation);
				}

				private ActiveRule getActiveRule(SensorContext context, InputFile inputFile) {
					String languageKey = inputFile.language();
					RuleKey ruleKey = languageKey==null 
						? RuleKey.of(FortifyConstants.FTFY_RULE_REPO_KEY(FortifyConstants.FTFY_LANGUAGE_KEY), FortifyConstants.FTFY_RULE_KEY(FortifyConstants.FTFY_LANGUAGE_KEY)) 
						: RuleKey.of(FortifyConstants.FTFY_RULE_REPO_KEY(languageKey), FortifyConstants.FTFY_RULE_KEY(languageKey));
					return ruleKey==null ? null : context.activeRules().find(ruleKey);
				}

				protected String getIssueMessage(SensorContext sensorContext, JSONMap issue) {
					return issue.get("issueName", String.class)+" ("+issue.get("deepLink", String.class)+")";
				}
				
				protected final InputFile getInputFile(FileSystem fs, JSONMap issue) {
					Path fullFileName = Paths.get(issue.get("fullFileName", String.class));
					Iterable<InputFile> files = fs.inputFiles(fs.predicates().all());
					for ( InputFile inputFile : files ) {
						Path path = inputFile.path();
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
	private JSONMap getSonarQubeFilterSet(SensorContext context, FortifySSCConnectionFactory connFactory) {
		JSONMap filterSet = null;
		String filterSetGuidOrTitle = context.settings().getString(FortifyConstants.PRP_FILTER_SET);
		if ( StringUtils.isNotBlank(filterSetGuidOrTitle) ) {
			String matchExpr = MessageFormat.format("guid==''{0}'' || title==''{0}''", new Object[]{filterSetGuidOrTitle});
			filterSet = connFactory.getApplicationVersion().get("filterSets", JSONList.class).find(matchExpr, true, JSONMap.class);
			if ( filterSet==null ) {
				throw new IllegalArgumentException("Unknown filter set "+filterSetGuidOrTitle);
			}
		}
		return filterSet==null ? getSSCDefaultFilterSet(connFactory) : filterSet;
	}
	
	private JSONMap getSSCDefaultFilterSet(FortifySSCConnectionFactory connFactory) {
		return connFactory.getApplicationVersion().get("filterSets", JSONList.class).find("defaultFilterSet", true, JSONMap.class);
	}

	/**
	 * Add 1 issue to the issue count for the given friority
	 * @param friority for which to add an issue to the issue count
	 */
	private void addIssueCount(String friority) {
		Integer count = issueCounts.get(friority);
		if ( count == null ) {
			count = 0;
		}
		issueCounts.put(friority, count+1);
	}

	/**
	 * Retrieve vulnerability data from SSC, and process each vulnerability using the given {@link IFortifyIssueProcessor}
	 * @param conn
	 * @param processor
	 */
	protected void processFortifyIssues(FortifySSCConnectionFactory connFactory, JSONMap filterSet, IJSONMapProcessor processor) {
		connFactory.getConnectionWithArtifactProcessing().api().issue().queryIssues(connFactory.getApplicationVersionId())
			.paramFilterSet(filterSet.get("guid",String.class))
			.paramQAnd("hidden", "false")
			.paramQAnd("suppressed", "false")
			.paramQAnd("engineType", "SCA").build().processAll(processor);
	}

	/**
	 * @param context
	 * @return true if SSC connection is available and issue collection is enabled, false otherwise
	 */
	private final boolean isActive(SensorContext context) {
		// Check whether connection is available
		boolean result = getConnFactory().isConnectionAvailable();
		// Check whether issue collection is enabled
		result &= context.settings().getBoolean(FortifyConstants.PRP_ENABLE_ISSUES);
		// TODO Check whether there are any active Fortify rules for any language
		return result;
	}
	
	private class IssueCountMetricValueRetriever extends AbstractMetricValueRetriever {
		private final String friority;
		public IssueCountMetricValueRetriever(String friority) {
			this.friority = friority;
		}
		@Override
		public Serializable getMetricValue(SensorContext context, Metric<Serializable> metric, FortifySSCConnectionFactory connFactory) {
			Integer value = issueCounts.get(friority);
			return value==null?0:value;
		}
	}

	/**
	 * This implementation simply returns the class name
	 */
	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
