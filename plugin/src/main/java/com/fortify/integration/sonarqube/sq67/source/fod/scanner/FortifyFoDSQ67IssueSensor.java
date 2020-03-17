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
package com.fortify.integration.sonarqube.sq67.source.fod.scanner;

import org.sonar.api.Startable;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;

import com.fortify.integration.sonarqube.common.issue.FortifyIssuesProcessor;
import com.fortify.integration.sonarqube.common.issue.FortifyIssuesProcessor.CacheHelper;
import com.fortify.integration.sonarqube.common.issue.IFortifySourceSystemIssueFieldRetriever;
import com.fortify.integration.sonarqube.common.source.fod.issue.FortifyFoDIssueFieldsRetriever;
import com.fortify.integration.sonarqube.common.source.fod.issue.FortifyFoDIssueQueryHelper;
import com.fortify.integration.sonarqube.common.source.fod.scanner.IFortifyFoDScannerSideConnectionHelper;
import com.fortify.integration.sonarqube.sq67.issue.FortifySQ67IssueJSONMapProcessorFactory;
import com.fortify.integration.sonarqube.sq67.scanner.FortifySQ67AbstractSensor;
import com.fortify.integration.sonarqube.sq67.scanner.FortifySQ67IssueSensorProperties;

/**
 * This {@link FortifySQ67AbstractSensor} implementation retrieves vulnerability data from FoD and
 * reports these vulnerabilities as SonarQube issues.
 * 
 * TODO Add more JavaDoc
 * 
 * @author Ruud Senden
 *
 */

/*
 * TODO Add plugin page that shows any Fortify issues that could not be matched to a SonarQube source file
 */
public class FortifyFoDSQ67IssueSensor extends FortifySQ67AbstractSensor<IFortifyFoDScannerSideConnectionHelper> implements Startable {
	private final FortifySQ67IssueSensorProperties sensorProperties;
	private final CacheHelper cacheHelper; 
	private final FortifyIssuesProcessor issuesProcessor;
	
	/**
	 * Constructor for injecting dependencies
	 * @param connFactory
	 */
	public FortifyFoDSQ67IssueSensor(IFortifyFoDScannerSideConnectionHelper connHelper, FortifySQ67IssueSensorProperties sensorProperties) {
		super(connHelper);
		this.sensorProperties = sensorProperties;
		IFortifySourceSystemIssueFieldRetriever issueFieldRetriever = new FortifyFoDIssueFieldsRetriever();
		this.cacheHelper = new CacheHelper(issueFieldRetriever, sensorProperties.isReportIssuesOnce());
		this.issuesProcessor = new FortifyIssuesProcessor(
				new FortifyFoDIssueQueryHelper(getConnHelper()), 
				new FortifySQ67IssueJSONMapProcessorFactory(issueFieldRetriever), cacheHelper);
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
	
	@Override
	public void start() {
		// Nothing to do
	}

	@Override
	public void stop() {
		if ( cacheHelper != null ) {
			cacheHelper.close();
		}
	}
}