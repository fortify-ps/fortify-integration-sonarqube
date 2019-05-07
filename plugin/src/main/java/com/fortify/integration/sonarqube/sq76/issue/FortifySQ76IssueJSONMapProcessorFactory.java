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
package com.fortify.integration.sonarqube.sq76.issue;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.scanner.fs.InputProject;

import com.fortify.integration.sonarqube.common.issue.AbstractFortifyIssueJSONMapProcessorFactory;
import com.fortify.integration.sonarqube.common.issue.FortifyIssuesProcessor.CacheHelper;
import com.fortify.integration.sonarqube.common.issue.IFortifySourceSystemIssueFieldRetriever;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.processor.IJSONMapProcessor;

public class FortifySQ76IssueJSONMapProcessorFactory extends AbstractFortifyIssueJSONMapProcessorFactory {
	public FortifySQ76IssueJSONMapProcessorFactory(IFortifySourceSystemIssueFieldRetriever issueFieldRetriever) {
		super(issueFieldRetriever);
	}

	@Override
	public IJSONMapProcessor getProcessor(SensorContext context, ActiveRule activeRule, CacheHelper cacheHelper) {
		return new FortifySQ76IssueJSONMapProcessor(context, activeRule, getIssueFieldRetriever(), cacheHelper);
	}
	
	private static final class FortifySQ76IssueJSONMapProcessor extends AbstractFortifyIssueJSONMapProcessor {
		public FortifySQ76IssueJSONMapProcessor(SensorContext context, ActiveRule activeRule, IFortifySourceSystemIssueFieldRetriever issueFieldRetriever, CacheHelper cacheHelper) {
			super(context, activeRule, issueFieldRetriever, cacheHelper);
		}
		
		@Override
		protected void createIssueWithoutInputFile(JSONMap issue) {
			if ( cacheHelper!=null ) { cacheHelper.addProcessedIssue(context, activeRule, issue); }
			NewIssue newIssue = createNewIssue(issue);
			addIssueLocation(newIssue, context.project(), issue);
			newIssue.save();
		}

		protected void addIssueLocation(NewIssue newIssue, InputProject inputProject, JSONMap issue) {
			NewIssueLocation primaryLocation = newIssue.newLocation()
					.on(inputProject)
					.message(getIssueMessage(issue, true));
			newIssue.at(primaryLocation);
		}
	}

}
