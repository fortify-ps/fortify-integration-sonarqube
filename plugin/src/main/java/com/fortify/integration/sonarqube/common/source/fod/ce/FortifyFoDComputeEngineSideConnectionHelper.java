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
package com.fortify.integration.sonarqube.common.source.fod.ce;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer.MeasureComputerContext;

import com.fortify.client.fod.connection.FoDAuthenticatingRestConnection;
import com.fortify.integration.sonarqube.common.ce.IFortifyComputeEngineSideConnectionHelper;
import com.fortify.integration.sonarqube.common.source.fod.IFortifyFoDConnectionHelper;
import com.fortify.integration.sonarqube.common.source.fod.metrics.FortifyFoDConnectionPropertiesMetrics;

/**
 * <p>FoD connection helper for ComputeEngine-side to get FoD connection instance and
 * release id.</p>
 * 
 * <p>Instead of getting the connection properties from the SonarQube configuration, we
 * have a sensor on the scanner side provide the relevant properties as SonarQube
 * measures for the following two reasons:</p>
 * <ul>
 *   <li>Connection properties like FoD URL and release name or id may not have been 
 *       configured on the SonarQube server, but instead being provided on the scanner 
 *       command line.</li>
 *   <li>Even if connection properties have been configured on the SonarQube server,
 *       these could have been overridden on the scanner command line; we want the
 *       compute engine to use the same connection properties as used on the scanner
 *       command line.</li>
 * </ul> 
 * 
 * @author Ruud Senden
 *
 */
public final class FortifyFoDComputeEngineSideConnectionHelper implements IFortifyFoDConnectionHelper, IFortifyComputeEngineSideConnectionHelper<FoDAuthenticatingRestConnection> {
	private final MeasureComputerContext measureComputerContext;
	private FoDAuthenticatingRestConnection connection = null;
	
	/**
	 * Constructor for injecting dependencies
	 * @param measureComputerContext
	 */
	public FortifyFoDComputeEngineSideConnectionHelper(MeasureComputerContext measureComputerContext) {
		this.measureComputerContext = measureComputerContext;
	}
	
	/**
	 * Get the input metric keys, containing the FoD URL and release id
	 * @return
	 */
	public static final String[] getInputMetricKeys() {
		return FortifyFoDConnectionPropertiesMetrics.METRICS_KEYS;
	}
	
	/**
	 * Get the FoD URL including credentials from the measure saved on the scanner side
	 */
	public final String getFoDUrl() {
		Measure measure = measureComputerContext.getMeasure(FortifyFoDConnectionPropertiesMetrics.PRP_FOD_URL);
		return measure == null ? null : measure.getStringValue();
	}
	
	@Override
	public String getFoDTenant() {
		Measure measure = measureComputerContext.getMeasure(FortifyFoDConnectionPropertiesMetrics.PRP_FOD_TENANT);
		return measure == null ? null : measure.getStringValue();
	}
	
	@Override
	public String getFoDUser() {
		Measure measure = measureComputerContext.getMeasure(FortifyFoDConnectionPropertiesMetrics.PRP_FOD_USER);
		return measure == null ? null : measure.getStringValue();
	}
	
	@Override
	public String getFoDPassword() {
		Measure measure = measureComputerContext.getMeasure(FortifyFoDConnectionPropertiesMetrics.PRP_FOD_PWD);
		return measure == null ? null : measure.getStringValue();
	}
	
	/**
	 * Get the release id from the measure saved on the scanner side
	 */
	public final String getReleaseId() {
		Measure measure = measureComputerContext.getMeasure(FortifyFoDConnectionPropertiesMetrics.PRP_FOD_RELEASE_ID);
		return measure == null ? null : measure.getStringValue();
	}
	
	/**
	 * Get the {@link FoDAuthenticatingRestConnection} based on the URL returned by {@link #getFoDUrl()}.
	 * The connection instance is cached for this {@link FortifyFoDComputeEngineSideConnectionHelper}
	 * instance. If the connection is not available, this method returns null.
	 */
	public final synchronized FoDAuthenticatingRestConnection getConnection() {
		String fodUrl = getFoDUrl();
		if ( connection==null && StringUtils.isNotBlank(fodUrl) && StringUtils.isNotBlank(getFoDTenant()) && StringUtils.isNotBlank(getFoDUser()) && StringUtils.isNotBlank(getFoDPassword())) {
			connection = FoDAuthenticatingRestConnection.builder().baseUrl(fodUrl).tenant(getFoDTenant()).userName(getFoDUser()).password(getFoDPassword()).build();
		}
		return connection;
	}
	
	/**
	 * This method indicates whether FoD connection and release id 
	 * are available. 
	 */
	public final boolean isConnectionAvailable() {
		return getConnection()!=null && getReleaseId()!=null; 
	}
}
