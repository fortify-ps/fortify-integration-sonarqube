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
package com.fortify.integration.sonarqube.sq76.issue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.RuleType;
import org.sonar.api.scanner.fs.InputProject;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.integration.sonarqube.common.issue.AbstractFortifyIssueJSONMapProcessorFactory;
import com.fortify.integration.sonarqube.common.issue.FortifyIssuesProcessor.CacheHelper;
import com.fortify.integration.sonarqube.common.issue.IFortifyIssueInputFileRetriever;
import com.fortify.integration.sonarqube.common.issue.IFortifyIssueRuleKeysRetriever;
import com.fortify.integration.sonarqube.common.issue.IFortifySourceSystemIssueFieldRetriever;
import com.fortify.integration.sonarqube.sq76.scanner.FortifySQ76IssueSensorProperties;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.processor.IJSONMapProcessor;

public class FortifySQ76IssueJSONMapProcessorFactory extends AbstractFortifyIssueJSONMapProcessorFactory {
	private final AdHocRulesHelper adHocRulesHelper = new AdHocRulesHelper();
	private final FortifySQ76IssueSensorProperties properties;
	public FortifySQ76IssueJSONMapProcessorFactory(IFortifySourceSystemIssueFieldRetriever issueFieldRetriever, FortifySQ76IssueSensorProperties sensorProperties) {
		super(issueFieldRetriever);
		this.properties = sensorProperties;
	}

	@Override
	public IJSONMapProcessor getProcessor(SensorContext context, IFortifyIssueRuleKeysRetriever issueRuleKeysRetriever, IFortifyIssueInputFileRetriever issueInputFileRetriever, CacheHelper cacheHelper) {
		return new FortifySQ76IssueJSONMapProcessor(context, issueRuleKeysRetriever, getIssueFieldRetriever(), issueInputFileRetriever, cacheHelper, adHocRulesHelper, properties);
	}
	
	private static final class FortifySQ76IssueJSONMapProcessor extends AbstractFortifyIssueJSONMapProcessor {
		private static final Logger LOG = Loggers.get(FortifySQ76IssueJSONMapProcessor.class);
		private final Set<String> debugRulesHtmlProcessedCategories = new HashSet<>(); 
		private final AdHocRulesHelper adHocRulesHelper;
		private final FortifySQ76IssueSensorProperties properties;
		public FortifySQ76IssueJSONMapProcessor(SensorContext context, IFortifyIssueRuleKeysRetriever issueRuleKeysRetriever, IFortifySourceSystemIssueFieldRetriever issueFieldRetriever, IFortifyIssueInputFileRetriever issueInputFileRetriever, CacheHelper cacheHelper, AdHocRulesHelper adHocRulesHelper, FortifySQ76IssueSensorProperties properties) {
			super(context, issueRuleKeysRetriever, issueFieldRetriever, issueInputFileRetriever, cacheHelper);
			this.adHocRulesHelper = adHocRulesHelper;
			this.properties = properties;
		}
		
		@Override
		protected void createIssuesWithoutInputFile(JSONMap issue) {
			writeDebugRulesHtml(issue);
			try {
				for ( RuleKey ruleKey : issueRuleKeysRetriever.getRuleKeys(issueFieldRetriever, issue) ) {
					NewIssue newIssue = createNewIssue(ruleKey, issue)
						.overrideSeverity(issueFieldRetriever.getSeverity(issue));
					newIssue.at(updateIssueLocation(newIssue.newLocation(), context.project(), issue));
					// TODO Low: Add .addFlow(Fortify evidence flow)
					newIssue.save();
				}
			} finally {
				if ( cacheHelper!=null ) { cacheHelper.addProcessedIssue(context, issue); }
			}
		}
		
		@Override
		protected void createIssuesOnInputFile(InputFile inputFile, JSONMap issue) {
			writeDebugRulesHtml(issue);
			if ( properties.useAdHocRules() ) {
				// TODO Should we only create ad hoc issues if corresponding standard rules are enabled?
				createAdHocIssue(inputFile, issue);
			}
			if ( properties.useStandardRules() ) {
				super.createIssuesOnInputFile(inputFile, issue);
			}
		}

		protected NewIssueLocation updateIssueLocation(NewIssueLocation newIssueLocation, InputProject inputProject, JSONMap issue) {
			return newIssueLocation
					.on(inputProject)
					.message(getIssueMessage(issue, true));
		}
		
		protected void createAdHocIssue(InputFile inputFile, JSONMap issue) {
			NewExternalIssue newIssue = context.newExternalIssue()
				.engineId(adHocRulesHelper.getEngineId())
				.ruleId(adHocRulesHelper.getAdHocRuleId(context, issueFieldRetriever, issue))
				.severity(issueFieldRetriever.getSeverity(issue))
				.type(RuleType.VULNERABILITY);
			newIssue.at(updateIssueLocation(newIssue.newLocation(), inputFile, issue));
			newIssue.save();
		}
		
		private void writeDebugRulesHtml(JSONMap issue) {
			String rulesHtmlFile = properties.getDebugRulesHtmlOutputFile();
			if ( rulesHtmlFile!=null ) {
				String category = issueFieldRetriever.getCategory(issue);
				if ( !debugRulesHtmlProcessedCategories.contains(category) ) {
					try {
						StandardOpenOption[] openOptions = debugRulesHtmlProcessedCategories.isEmpty()
								? new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE}
								: new StandardOpenOption[]{StandardOpenOption.APPEND, StandardOpenOption.WRITE};
						Files.write(Paths.get(rulesHtmlFile), 
							("<h1>"+category+"</h1>"+issueFieldRetriever.getRuleDescription(issue)).getBytes(), 
							openOptions);
					} catch (IOException e) {
						LOG.error("Error writing to "+rulesHtmlFile, e);
					} finally {
						debugRulesHtmlProcessedCategories.add(category);
					}
				}
			}
		}
	}
	
	private static final class AdHocRulesHelper {
		private final Set<String> adHocRuleIds = new HashSet<>();
		
		public String getEngineId() {
			return "Fortify";
		}
		
		public String getAdHocRuleId(SensorContext context, IFortifySourceSystemIssueFieldRetriever issueFieldRetriever, JSONMap issue) {
			String ruleId = issueFieldRetriever.getCategory(issue);
			if ( !adHocRuleIds.contains(ruleId) ) {
				context.newAdHocRule()
					.engineId(getEngineId())
					.ruleId(ruleId)
					.name(ruleId)
					.description(issueFieldRetriever.getRuleDescription(issue))
					.type(RuleType.VULNERABILITY)
					.severity(issueFieldRetriever.getSeverity(issue))
					.save();
			}
			return ruleId;
		}
		
	}

}
