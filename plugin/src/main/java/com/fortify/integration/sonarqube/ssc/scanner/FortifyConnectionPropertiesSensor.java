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
package com.fortify.integration.sonarqube.ssc.scanner;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

@SuppressWarnings({"unchecked", "rawtypes"})
public class FortifyConnectionPropertiesSensor implements Sensor {
	public static final String PRP_SSC_URL = "fortify.sscUrl";
	public static final String PRP_APP_VERSION_ID = "fortify.sscApplicationVersionId";
	private static final Metric METRIC_SSC_URL = new Metric.Builder(PRP_SSC_URL, "SSC URL", Metric.ValueType.STRING)
			.setDomain("Fortify").setHidden(true).create();
	private static final Metric METRIC_SSC_APP_VERSION_ID = new Metric.Builder(PRP_APP_VERSION_ID, "SSC Application Version Id", Metric.ValueType.STRING)
			.setDomain("Fortify").setHidden(true).create();
	
	private static final List<Metric> METRICS = Arrays.asList(new Metric[] {METRIC_SSC_URL, METRIC_SSC_APP_VERSION_ID});
	
	private final FortifySSCScannerSideConnectionHelper connHelper;
	
	public FortifyConnectionPropertiesSensor(FortifySSCScannerSideConnectionHelper connHelper) {
		this.connHelper = connHelper;
	}
	
	@Override
	public void describe(SensorDescriptor descriptor) {
		descriptor.name("Set connection properties for compute engine");
	}

	@Override
	public void execute(SensorContext context) {
		context.newMeasure().forMetric(METRIC_SSC_URL).on(context.module()).withValue(connHelper.getSSCUrl()).save();
		context.newMeasure().forMetric(METRIC_SSC_APP_VERSION_ID).on(context.module()).withValue(connHelper.getApplicationVersionId()).save();
	}

	public static final class MetricsImpl implements Metrics {
		@Override
		public List<Metric> getMetrics() {
			return METRICS;
		}
	}
}
