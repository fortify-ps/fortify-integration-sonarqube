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
package com.fortify.integration.sonarqube.ssc.common.ce;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.ce.measure.MeasureComputer.MeasureComputerContext;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;

/**
 * SSC connection helper for ComputeEngine-side to get SSC connection instance and
 * application version id. This abstract class provides all relevant functionality,
 * but concrete implementations must provide a concrete SensorImpl class for adding
 * measures for a specific SonarQube version.
 * 
 * @author Ruud Senden
 *
 */
@SuppressWarnings({"rawtypes"})
public abstract class AbstractFortifyComputeEngineSideConnectionHelper implements IFortifyComputeEngineSideConnectionHelper {
	private static final String PRP_SSC_URL = "fortify.sscUrl";
	private static final String PRP_APP_VERSION_ID = "fortify.sscApplicationVersionId";
	protected static final Metric METRIC_SSC_URL = new Metric.Builder(PRP_SSC_URL, "SSC URL", Metric.ValueType.STRING)
			.setDomain("Fortify").setHidden(true).create();
	protected static final Metric METRIC_SSC_APP_VERSION_ID = new Metric.Builder(PRP_APP_VERSION_ID, "SSC Application Version Id", Metric.ValueType.STRING)
			.setDomain("Fortify").setHidden(true).create();
	
	private static final List<Metric> METRICS = Arrays.asList(new Metric[] {METRIC_SSC_URL, METRIC_SSC_APP_VERSION_ID});
	
	private final MeasureComputerContext measureComputerContext;
	private SSCAuthenticatingRestConnection connection = null;
	
	public AbstractFortifyComputeEngineSideConnectionHelper(MeasureComputerContext measureComputerContext) {
		this.measureComputerContext = measureComputerContext;
	}
	
	public static final String[] getInputMetricKeys() {
		return new String[] {PRP_SSC_URL, PRP_APP_VERSION_ID};
	}
	
	public final String getSSCUrl() {
		return measureComputerContext.getMeasure(AbstractFortifyComputeEngineSideConnectionHelper.PRP_SSC_URL).getStringValue();
	}
	
	public final String getApplicationVersionId() {
		return measureComputerContext.getMeasure(AbstractFortifyComputeEngineSideConnectionHelper.PRP_APP_VERSION_ID).getStringValue();
	}
	
	public final synchronized SSCAuthenticatingRestConnection getConnection() {
		String sscUrl = getSSCUrl();
		if ( connection==null && StringUtils.isNotBlank(sscUrl) ) {
			connection = SSCAuthenticatingRestConnection.builder().baseUrl(getSSCUrl()).build();
		}
		return connection;
	}
	
	public final boolean isConnectionAvailable() {
		return getConnection()!=null && getApplicationVersionId()!=null; 
	}

	public static final class MetricsImpl implements Metrics {
		@Override
		public List<Metric> getMetrics() {
			return METRICS;
		}
	}
}
