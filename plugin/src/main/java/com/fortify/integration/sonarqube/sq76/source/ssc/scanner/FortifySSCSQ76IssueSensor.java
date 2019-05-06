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
package com.fortify.integration.sonarqube.sq76.source.ssc.scanner;

import java.util.List;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.scanner.fs.InputProject;

import com.fortify.integration.sonarqube.common.source.ssc.scanner.AbstractFortifySSCIssueSensorProperties;
import com.fortify.integration.sonarqube.common.source.ssc.scanner.FortifySSCIssueSensorHelper;
import com.fortify.integration.sonarqube.common.source.ssc.scanner.IFortifySSCScannerSideConnectionHelper;
import com.fortify.util.rest.json.JSONMap;


/**
 * This {@link FortifySSCSQ76AbstractProjectSensor} implementation retrieves vulnerability data from SSC and
 * reports these vulnerabilities as SonarQube issues.
 * 
 * TODO Add more JavaDoc
 * 
 * @author Ruud Senden
 *
 */
@ScannerSide
public class FortifySSCSQ76IssueSensor extends FortifySSCSQ76AbstractProjectSensor {
	private final SensorProperties sensorProperties;
	
	/**
	 * Constructor for injecting dependencies
	 * @param connFactory
	 */
	public FortifySSCSQ76IssueSensor(IFortifySSCScannerSideConnectionHelper connHelper, SensorProperties sensorProperties) {
		super(connHelper);
		this.sensorProperties = sensorProperties;
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
		new FortifyIssuesProcessor(context, getConnHelper(), sensorProperties).processIssues();
	}

	/**
	 * @param context
	 * @return true if SSC connection is available and issue collection is enabled, false otherwise
	 */
	@Override
	protected final boolean isActive(SensorContext context) {
		return sensorProperties.isIssueCollectionEnabled(context);
	}
	
	@ScannerSide
	public static final class SensorProperties extends AbstractFortifySSCIssueSensorProperties {
		public SensorProperties(IFortifySSCScannerSideConnectionHelper connHelper) {
			super(connHelper);
		}
	}
	
	private static final class FortifyIssuesProcessor extends FortifySSCIssueSensorHelper.AbstractFortifyIssuesProcessor<SensorProperties> {
		public FortifyIssuesProcessor(SensorContext context, IFortifySSCScannerSideConnectionHelper connHelper, SensorProperties sensorProperties) {
			super(context, connHelper, sensorProperties);
		}

		@Override
		public void processAllIssues(ActiveRule activeRule) {
			getIssuesBaseQuery().build()
				.processAll(new FortifyIssueProcessor(getContext(), activeRule, getInputFiles()));
		}
		
		@Override
		public void processIssuesForExternalCategory(ActiveRule activeRule, String externalListId, String externalCategory) {
			getIssuesBaseQuery(externalListId, externalCategory).build()
				.processAll(new FortifyIssueProcessor(getContext(), activeRule, getInputFiles()));
		}
	}
	
	private static final class FortifyIssueProcessor extends FortifySSCIssueSensorHelper.AbstractFortifyIssueProcessor {
		public FortifyIssueProcessor(SensorContext context, ActiveRule activeRule, List<InputFile> inputFiles) {
			super(context, activeRule, inputFiles);
		}

		@Override
		protected void createIssueWithoutInputFile(JSONMap issue) {
			NewIssue newIssue = createNewIssue(issue);
			addIssueLocation(newIssue, getContext().project(), issue);
			newIssue.save();
		}

		protected void addIssueLocation(NewIssue newIssue, InputProject inputProject, JSONMap issue) {
			NewIssueLocation primaryLocation = newIssue.newLocation()
					.on(inputProject)
					.message(getIssueMessage(issue, true)); //TODO Add Fortify issue location to message
			newIssue.at(primaryLocation);
		}
	}
}