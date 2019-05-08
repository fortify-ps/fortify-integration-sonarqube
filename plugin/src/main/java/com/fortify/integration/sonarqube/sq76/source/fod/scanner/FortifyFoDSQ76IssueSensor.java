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
package com.fortify.integration.sonarqube.sq76.source.fod.scanner;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.scanner.ScannerSide;

import com.fortify.integration.sonarqube.common.issue.FortifyIssuesProcessor;
import com.fortify.integration.sonarqube.common.source.fod.issue.FortifyFoDIssueFieldsRetriever;
import com.fortify.integration.sonarqube.common.source.fod.issue.FortifyFoDIssueQueryHelper;
import com.fortify.integration.sonarqube.common.source.fod.scanner.IFortifyFoDScannerSideConnectionHelper;
import com.fortify.integration.sonarqube.sq76.issue.FortifySQ76IssueJSONMapProcessorFactory;
import com.fortify.integration.sonarqube.sq76.scanner.FortifySQ76AbstractProjectSensor;
import com.fortify.integration.sonarqube.sq76.scanner.FortifySQ76IssueSensorProperties;


/**
 * This {@link FortifySQ76AbstractProjectSensor} implementation retrieves vulnerability data from FoD and
 * reports these vulnerabilities as SonarQube issues.
 * 
 * TODO Add more JavaDoc
 * 
 * @author Ruud Senden
 *
 */
@ScannerSide
public class FortifyFoDSQ76IssueSensor extends FortifySQ76AbstractProjectSensor<IFortifyFoDScannerSideConnectionHelper> {
	private final FortifySQ76IssueSensorProperties sensorProperties;
	private final FortifyIssuesProcessor issuesProcessor;
	
	/**
	 * Constructor for injecting dependencies
	 * @param connFactory
	 */
	public FortifyFoDSQ76IssueSensor(IFortifyFoDScannerSideConnectionHelper connHelper, FortifySQ76IssueSensorProperties sensorProperties) {
		super(connHelper);
		this.sensorProperties = sensorProperties;
		this.issuesProcessor = new FortifyIssuesProcessor(
				new FortifyFoDIssueQueryHelper(getConnHelper()), 
				new FortifySQ76IssueJSONMapProcessorFactory(new FortifyFoDIssueFieldsRetriever(), sensorProperties));
	}
	
	@Override
	public void describe(SensorDescriptor descriptor) {
		descriptor.name("Fortify FoD issue collection");
	}
	
	/**
	 * Sensor implementation that retrieves issue details from SSC, and reports them as SonarQube violations
	 */
	@Override
	public void _execute(SensorContext context) {
		issuesProcessor.processIssues(context);
	}

	/**
	 * @param context
	 * @return true if SSC connection is available and issue collection is enabled, false otherwise
	 */
	@Override
	protected final boolean isActive(SensorContext context) {
		return sensorProperties.isIssueCollectionEnabled();
	}
}