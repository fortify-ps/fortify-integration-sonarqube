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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.integration.sonarqube.common.FortifyConstants;
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
		protected final List<InputFile> inputFiles;
		protected final ActiveRule activeRule;
		protected final IFortifySourceSystemIssueFieldRetriever issueFieldRetriever;
		protected final CacheHelper cacheHelper;
		
		public AbstractFortifyIssueJSONMapProcessor(SensorContext context, ActiveRule activeRule, IFortifySourceSystemIssueFieldRetriever issueFieldRetriever, CacheHelper cacheHelper) {
			this.context = context;
			this.inputFiles = getInputFilesSortedByPathLength(context.fileSystem());
			this.activeRule = activeRule;
			this.issueFieldRetriever = issueFieldRetriever;
			this.cacheHelper = cacheHelper;
		}

		@Override
		public final void process(JSONMap issue) {
			if ( !ignoreIssue(issue) ) {
				try {
					// Get the inputFile for the current issue
					InputFile inputFile = getInputFileForIssue(inputFiles, issue);
					if ( inputFile==null ) {
						LOG.debug("No InputFile found for "+issueFieldRetriever.getFileName(issue)+" (vulnerability id "+issueFieldRetriever.getId(issue)+")");
						createIssueWithoutInputFile(issue);
					} else {
						LOG.debug("InputFile found for "+issueFieldRetriever.getFileName(issue)+" (vulnerability id "+issueFieldRetriever.getId(issue)+")");
						createIssueOnInputFile(inputFile, issue);
					}
				} catch ( RuntimeException e ) {
					LOG.error("Error creating SonarQube issue for vulnerability id "+issueFieldRetriever.getId(issue), e);
				}
			}
		}
		
		protected boolean ignoreIssue(JSONMap issue) {
			return cacheHelper!=null && cacheHelper.hasProcessedIssue(activeRule, issue);
		}
		
		protected void createIssueWithoutInputFile(JSONMap issue) {}
		
		protected void createIssueOnInputFile(InputFile inputFile, JSONMap issue) {
			if ( cacheHelper!=null ) { cacheHelper.addProcessedIssue(context, activeRule, issue); }
			NewIssue newIssue = createNewIssue(issue);
			addIssueLocation(newIssue, inputFile, issue);
			// TODO Low: Add .addFlow(Fortify evidence flow)
			newIssue.save();
		}

		protected NewIssue createNewIssue(JSONMap issue) {
			String friority = StringUtils.lowerCase(issueFieldRetriever.getFriority(issue));
			NewIssue newIssue = context.newIssue().forRule(activeRule.ruleKey());
			newIssue.overrideSeverity(FortifyConstants.FRIORITY_TO_SEVERITY(friority));
			return newIssue;
		}

		protected void addIssueLocation(NewIssue newIssue, InputFile inputFile, JSONMap issue) {
			int lineNumber = Math.max(1, issueFieldRetriever.getLineNumber(issue));
			NewIssueLocation primaryLocation = newIssue.newLocation()
					.on(inputFile)
					.at(inputFile.selectLine(lineNumber))
					.message(getIssueMessage(issue, false));
			newIssue.at(primaryLocation);
		}

		protected String getIssueMessage(JSONMap issue, boolean includeFilename) {
			String prefix = !includeFilename ? "" : (issueFieldRetriever.getFileName(issue)+" - ");
			return prefix 
				+ issueFieldRetriever.getCategory(issue) 
				+ " ("+issueFieldRetriever.getDeepLink(issue)+")";
		}
		
		protected final InputFile getInputFileForIssue(List<InputFile> inputFiles, JSONMap issue) {
			String fortifyFileName = issueFieldRetriever.getFileName(issue);
			Path fortifyFilePath = null;
			try {
				fortifyFilePath = Paths.get(fortifyFileName);
			} catch ( RuntimeException e ) {
				// This can happen, for example, if the Fortify file name is a URL and thus cannot be parsed as Path
				LOG.debug("Unable to resolve input file for "+fortifyFileName+", returning null", e);
				return null;
			}
			for ( InputFile inputFile : inputFiles ) {
				Path path = inputFile.path();
				if ( path.endsWith(fortifyFilePath) ) {
					return inputFile;
				}
			}
			
			LOG.debug("No input file found for "+fortifyFileName+", returning null");
			return null;
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
	}
}
