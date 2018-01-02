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
package com.fortify.integration.sonarqube.ssc.batch;

import java.io.Serializable;
import java.util.Map;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.Metric;

import com.fortify.client.ssc.api.SSCMetricsAPI.MetricType;
import com.fortify.integration.sonarqube.ssc.FortifySSCConnectionFactory;
import com.fortify.integration.sonarqube.ssc.util.MetricValueTypeUtil;
import com.fortify.util.rest.json.JSONList;


/**
 * This {@link FortifyMetricsProvider} implementation provides {@link Metric} definitions
 * and corresponding {@link SSCSnapshotMetricValueRetriever} implementations for various Fortify
 * performance indicator and variable values. 
 * 
 * @author Ruud Senden
 *
 */
public final class FortifySnapshotMetricsAndSensor extends AbstractFortifyMetricsAndSensor {
	public FortifySnapshotMetricsAndSensor(FortifySSCConnectionFactory connFactory) {
		super(connFactory);
	}
	
	@Override
	public void addMetrics(Map<Metric<Serializable>, MetricValueRetriever> metricsMap) {
		addPerformanceIndicatorMetrics(metricsMap);
		addVariableMetrics(metricsMap);
	}

	private static final void addPerformanceIndicatorMetrics(Map<Metric<Serializable>, MetricValueRetriever> map) {
		map.put(new Metric.Builder("fortify.ssc.securityRating",
				"Fortify Security Rating (SSC)", Metric.ValueType.FLOAT).setDescription("Fortify Security Rating (SSC)")
				.setDirection(Metric.DIRECTION_BETTER).setQualitative(true).setDomain("Fortify")
				.create(), 
				new SSCSnapshotMetricValueRetriever(MetricType.performanceIndicator, "FortifySecurityRating"));
		
		map.put(new Metric.Builder("fortify.ssc.remediationEffort",
				"Total Remediation Effort (SSC)", Metric.ValueType.FLOAT).setDescription("Total Remediation Effort (SSC)")
				.setDirection(Metric.DIRECTION_BETTER).setQualitative(true).setDomain("Fortify")
				.create(), 
				new SSCSnapshotMetricValueRetriever(MetricType.performanceIndicator, "TotalRemediationEffort"));
		
		map.put(new Metric.Builder("fortify.ssc.ACFPO", "Audited Critical Priority Issues (SSC)",
				Metric.ValueType.PERCENT).setDescription("Audited Critical Issues (SSC)").setDirection(Metric.DIRECTION_BETTER)
				.setQualitative(true).setDomain("Fortify").create(), 
				new SSCSnapshotMetricValueRetriever(MetricType.performanceIndicator, "PercentCriticalPriorityIssuesAudited"));
		
		map.put(new Metric.Builder("fortify.ssc.AHFPO", "Audited High Priority Issues (SSC)",
				Metric.ValueType.PERCENT).setDescription("Audited High Priority Issues (SSC)")
				.setDirection(Metric.DIRECTION_BETTER).setQualitative(true).setDomain("Fortify")
				.create(), 
				new SSCSnapshotMetricValueRetriever(MetricType.performanceIndicator, "PercentHighPriorityIssuesAudited"));
	}

	private static final void addVariableMetrics(Map<Metric<Serializable>, MetricValueRetriever> map) {
		map.put(new Metric.Builder("fortify.ssc.CFPO", "Critical Priority Issues (SSC)",
				Metric.ValueType.INT).setDescription("Critical Issues (SSC)").setDirection(Metric.DIRECTION_WORST)
				.setQualitative(true).setDomain("Fortify").create(), 
				new SSCSnapshotMetricValueRetriever(MetricType.variable, "CFPO"));
		
		map.put(new Metric.Builder("fortify.ssc.HFPO", "High Priority Issues (SSC)",
				Metric.ValueType.INT).setDescription("High Priority Issues (SSC)").setDirection(Metric.DIRECTION_WORST)
				.setQualitative(true).setDomain("Fortify").create(), 
				new SSCSnapshotMetricValueRetriever(MetricType.variable, "HFPO"));
		
		map.put(new Metric.Builder("fortify.ssc.MFPO", "Medium Priority Issues (SSC)",
				Metric.ValueType.INT).setDescription("Medium Priority Issues (SSC)").setDirection(Metric.DIRECTION_WORST)
				.setQualitative(true).setDomain("Fortify").create(), 
				new SSCSnapshotMetricValueRetriever(MetricType.variable, "MFPO"));
		
		map.put(new Metric.Builder("fortify.ssc.LFPO", "Low Priority Issues (SSC)",
				Metric.ValueType.INT).setDescription("Low Priority Issues (SSC)").setDirection(Metric.DIRECTION_WORST)
				.setQualitative(true).setDomain("Fortify").create(), 
				new SSCSnapshotMetricValueRetriever(MetricType.variable, "LFPO"));
		
		map.put(new Metric.Builder("fortify.ssc.LOC", "Lines of Code (SSC)",
				Metric.ValueType.INT).setDescription("Lines of Code (SSC)").setDirection(Metric.DIRECTION_NONE)
				.setQualitative(false).setDomain("Fortify").create(), 
				new SSCSnapshotMetricValueRetriever(MetricType.variable, "LOC"));
		
		map.put(new Metric.Builder("fortify.ssc.ExecutableLOC", "Executable Lines of Code (SSC)",
				Metric.ValueType.INT).setDescription("Executable Lines of Code (SSC)").setDirection(Metric.DIRECTION_NONE)
				.setQualitative(false).setDomain("Fortify").create(), 
				new SSCSnapshotMetricValueRetriever(MetricType.variable, "ExecutableLOC"));
		
		map.put(new Metric.Builder("fortify.ssc.FILES", "Number of Files (SSC)",
				Metric.ValueType.INT).setDescription("Number of Files (SSC)").setDirection(Metric.DIRECTION_NONE)
				.setQualitative(false).setDomain("Fortify").create(), 
				new SSCSnapshotMetricValueRetriever(MetricType.variable, "FILES"));
	}
	
	private static final class SSCSnapshotMetricValueRetriever extends AbstractMetricValueRetriever {
		private final MetricType metricType;
		private final String id;
		public SSCSnapshotMetricValueRetriever(MetricType metricType, String id) {
			this.metricType = metricType;
			this.id = id;
		}
		@Override
		public Serializable getMetricValue(SensorContext context, Metric<Serializable> metric, FortifySSCConnectionFactory connFactory) {
			JSONList metrics = connFactory.getApplicationVersion().get(metricType.toString(), JSONList.class);
			return (Serializable) metrics.mapValue("id", this.id, "value", MetricValueTypeUtil.getValueTypeClass(metric));
		}
	}
}
