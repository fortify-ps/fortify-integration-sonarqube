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

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.util.rest.json.JSONMap;

public class FortifyIssueInputFileRetrieverPathBased implements IFortifyIssueInputFileRetriever {
	private static final Logger LOG = Loggers.get(FortifyIssueInputFileRetrieverPathBased.class);
	protected final List<InputFile> inputFiles;
	
	public FortifyIssueInputFileRetrieverPathBased(SensorContext context) {
		this.inputFiles = getInputFilesSortedByPathLength(context.fileSystem());
	}
	@Override
	public InputFile getInputFile(IFortifySourceSystemIssueFieldRetriever issueFieldRetriever, JSONMap issue) {
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
	private static final List<InputFile> getInputFilesSortedByPathLength(FileSystem fs) {
		// This uses deprecated SQ API, but there seems to be no non-deprecated methods for getting
		// the full file name; uri() is not deprecated but not guaranteed to return the actual file
		// location.
		List<InputFile> result = StreamSupport.stream(fs.inputFiles(fs.predicates().all()).spliterator(), false).collect(Collectors.toList());
		result.sort(Comparator.<InputFile>comparingInt(inputFile -> inputFile.path().toString().length()));
        return result;
	}

}
