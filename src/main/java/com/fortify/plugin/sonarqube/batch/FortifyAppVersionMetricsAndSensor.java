/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
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
package com.fortify.plugin.sonarqube.batch;

import java.io.Serializable;
import java.util.Map;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.Metric;

import com.fortify.plugin.sonarqube.FortifySSCConnectionFactory;

/**
 * This {@link FortifyMetricsProvider} implementation provides {@link Metric} definitions
 * and corresponding {@link MetricValueRetriever} implementations for Fortify application
 * version data, like SSC application version URL, application name and version name.
 * 
 * @author Ruud Senden
 *
 */
public final class FortifyAppVersionMetricsAndSensor extends AbstractFortifyMetricsAndSensor {
	
	public FortifyAppVersionMetricsAndSensor(FortifySSCConnectionFactory connFactory) {
		super(connFactory);
	}

	@Override
	public void addMetrics(Map<Metric<Serializable>, MetricValueRetriever> metricsMap) {
		metricsMap.put(new Metric.Builder("fortify.sscProjectUrl", "SSC Application Version URL",
				Metric.ValueType.STRING).setDescription("SSC Application VersionURL").setDirection(Metric.DIRECTION_NONE)
						.setQualitative(false).setDomain("Fortify").create(),
				new AbstractMetricValueRetriever() {
					@Override
					public Serializable getMetricValue(SensorContext context, Metric<Serializable> metric, FortifySSCConnectionFactory connFactory) {
						return connFactory.getApplicationVersion().get("deepLink",String.class);
					}
				});
		metricsMap.put(new Metric.Builder("fortify.sscAppName", "SSC Application Name",
				Metric.ValueType.STRING).setDescription("SSC Application Name").setDirection(Metric.DIRECTION_NONE)
						.setQualitative(false).setDomain("Fortify").create(),
				new AbstractMetricValueRetriever() {
					@Override
					public Serializable getMetricValue(SensorContext context, Metric<Serializable> metric, FortifySSCConnectionFactory connFactory) {
						return connFactory.getApplicationVersion().getPath("project.name",String.class);
					}
				});
		metricsMap.put(new Metric.Builder("fortify.sscVersionName", "SSC Version Name",
				Metric.ValueType.STRING).setDescription("SSC Version Name").setDirection(Metric.DIRECTION_NONE)
						.setQualitative(false).setDomain("Fortify").create(),
				new AbstractMetricValueRetriever() {
					@Override
					public Serializable getMetricValue(SensorContext context, Metric<Serializable> metric, FortifySSCConnectionFactory connFactory) {
						return connFactory.getApplicationVersion().get("name",String.class);
					}
				});
	}
}
