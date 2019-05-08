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

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.integration.sonarqube.common.issue.FortifyIssuesProcessor.CacheHelper;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.processor.AbstractJSONMapProcessor;

public abstract class AbstractFortifyIssueJSONMapProcessorFactory implements IFortifyIssueJSONMapProcessorFactory {
	private final IFortifySourceSystemIssueFieldRetriever issueFieldRetriever;

	public AbstractFortifyIssueJSONMapProcessorFactory(IFortifySourceSystemIssueFieldRetriever issueFieldRetriever) {
		this.issueFieldRetriever = issueFieldRetriever;
	}

	public IFortifySourceSystemIssueFieldRetriever getIssueFieldRetriever() {
		return issueFieldRetriever;
	}
	
	protected static abstract class AbstractFortifyIssueJSONMapProcessor extends AbstractJSONMapProcessor {
		private static final Logger LOG = Loggers.get(AbstractFortifyIssueJSONMapProcessor.class);
		protected final SensorContext context;
		protected final IFortifyIssueRuleKeysRetriever issueRuleKeysRetriever;
		protected final IFortifySourceSystemIssueFieldRetriever issueFieldRetriever;
		protected final IFortifyIssueInputFileRetriever issueInputFileRetriever;
		protected final CacheHelper cacheHelper;
		
		public AbstractFortifyIssueJSONMapProcessor(SensorContext context, IFortifyIssueRuleKeysRetriever issueRuleKeysRetriever, IFortifySourceSystemIssueFieldRetriever issueFieldRetriever, IFortifyIssueInputFileRetriever issueInputFileRetriever, CacheHelper cacheHelper) {
			this.context = context;
			this.issueRuleKeysRetriever = issueRuleKeysRetriever;
			this.issueFieldRetriever = issueFieldRetriever;
			this.issueInputFileRetriever = issueInputFileRetriever;
			this.cacheHelper = cacheHelper;
		}

		@Override
		public final void process(JSONMap issue) {
			if ( !ignoreIssue(issue) ) {
				try {
					// Get the inputFile for the current issue
					InputFile inputFile = issueInputFileRetriever.getInputFile(issueFieldRetriever, issue);
					if ( inputFile==null ) {
						LOG.debug("No InputFile found for "+issueFieldRetriever.getFileName(issue)+" (vulnerability id "+issueFieldRetriever.getId(issue)+")");
						createIssuesWithoutInputFile(issue);
					} else {
						LOG.debug("InputFile found for "+issueFieldRetriever.getFileName(issue)+" (vulnerability id "+issueFieldRetriever.getId(issue)+")");
						createIssuesOnInputFile(inputFile, issue);
					}
				} catch ( RuntimeException e ) {
					LOG.error("Error creating SonarQube issue for vulnerability id "+issueFieldRetriever.getId(issue), e);
				}
			}
		}
		
		protected boolean ignoreIssue(JSONMap issue) {
			return cacheHelper!=null && cacheHelper.ignoreIssue(issue);
		}
		
		protected void createIssuesWithoutInputFile(JSONMap issue) {}
		
		protected void createIssuesOnInputFile(InputFile inputFile, JSONMap issue) {
			try {
				for ( RuleKey ruleKey : issueRuleKeysRetriever.getRuleKeys(issueFieldRetriever, issue) ) {
					NewIssue newIssue = createNewIssue(ruleKey, issue);
					newIssue.overrideSeverity(issueFieldRetriever.getSeverity(issue));
					newIssue.at(updateIssueLocation(newIssue.newLocation(), inputFile, issue));
					
					// TODO Low: Add .addFlow(Fortify evidence flow)
					newIssue.save();
				}
			} finally {
				if ( cacheHelper!=null ) { cacheHelper.addProcessedIssue(context, issue); }
			}
		}

		protected NewIssue createNewIssue(RuleKey ruleKey, JSONMap issue) {
			return context.newIssue().forRule(ruleKey);
		}

		protected NewIssueLocation updateIssueLocation(NewIssueLocation newIssueLocation, InputFile inputFile, JSONMap issue) {
			int lineNumber = Math.max(1, issueFieldRetriever.getLineNumber(issue));
			return newIssueLocation
					.on(inputFile)
					.at(inputFile.selectLine(lineNumber))
					.message(getIssueMessage(issue, false));
		}

		protected String getIssueMessage(JSONMap issue, boolean includeFilename) {
			String prefix = !includeFilename ? "" : (issueFieldRetriever.getFileName(issue)+" - ");
			return prefix 
				+ issueFieldRetriever.getCategory(issue) 
				+ " ("+issueFieldRetriever.getDeepLink(issue)+")";
		}
	}
}
