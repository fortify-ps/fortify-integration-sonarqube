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
package com.fortify.integration.sonarqube.ssc.ce;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.ce.measure.MeasureComputer.MeasureComputerContext;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.integration.sonarqube.ssc.scanner.FortifySSCScannerSideConnectionHelper;

@SuppressWarnings({"unchecked", "rawtypes"})
public class FortifySSCComputeEngineSideConnectionHelper {
	private static final String PRP_SSC_URL = "fortify.sscUrl";
	private static final String PRP_APP_VERSION_ID = "fortify.sscApplicationVersionId";
	private static final Metric METRIC_SSC_URL = new Metric.Builder(PRP_SSC_URL, "SSC URL", Metric.ValueType.STRING)
			.setDomain("Fortify").setHidden(true).create();
	private static final Metric METRIC_SSC_APP_VERSION_ID = new Metric.Builder(PRP_APP_VERSION_ID, "SSC Application Version Id", Metric.ValueType.STRING)
			.setDomain("Fortify").setHidden(true).create();
	
	private static final List<Metric> METRICS = Arrays.asList(new Metric[] {METRIC_SSC_URL, METRIC_SSC_APP_VERSION_ID});
	
	private final MeasureComputerContext measureComputerContext;
	private SSCAuthenticatingRestConnection connection = null;
	
	public FortifySSCComputeEngineSideConnectionHelper(MeasureComputerContext measureComputerContext) {
		this.measureComputerContext = measureComputerContext;
	}
	
	public static final String[] getInputMetricKeys() {
		return new String[] {PRP_SSC_URL, PRP_APP_VERSION_ID};
	}
	
	public final String getSSCUrl() {
		return measureComputerContext.getMeasure(FortifySSCComputeEngineSideConnectionHelper.PRP_SSC_URL).getStringValue();
	}
	
	public final String getApplicationVersionId() {
		return measureComputerContext.getMeasure(FortifySSCComputeEngineSideConnectionHelper.PRP_APP_VERSION_ID).getStringValue();
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
	
	public static final class SensorImpl implements Sensor {
		private final FortifySSCScannerSideConnectionHelper connHelper;
		
		public SensorImpl(FortifySSCScannerSideConnectionHelper connHelper) {
			this.connHelper = connHelper;
		}
		
		@Override
		public void describe(SensorDescriptor descriptor) {
			descriptor.name("Set connection properties for compute engine");
		}
	
		@Override
		public void execute(SensorContext context) {
			// TODO Verify whether this hidden measure can be retrieved in any way by users
			// that should not be able to see the SSC connection credentials. If so,
			// probably best to have the configuration utility generate a Yaml file with
			// a random shared secret to encrypt the URL/credentials here, and decrypt
			// this in the FortifySSCComputeEngineSideConnectionHelper.getSscUrl() method above.
			context.newMeasure().forMetric(METRIC_SSC_URL).on(context.module()).withValue(connHelper.getSSCUrl()).save();
			context.newMeasure().forMetric(METRIC_SSC_APP_VERSION_ID).on(context.module()).withValue(connHelper.getApplicationVersionId()).save();
		}
	}

	public static final class MetricsImpl implements Metrics {
		@Override
		public List<Metric> getMetrics() {
			return METRICS;
		}
	}
}
