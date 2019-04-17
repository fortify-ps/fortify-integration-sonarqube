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
package com.fortify.integration.sonarqube.ssc.metric.provider.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.Metric;

import com.fortify.client.ssc.api.SSCMetricsAPI.MetricType;
import com.fortify.integration.sonarqube.ssc.FortifySSCConnectionFactory;
import com.fortify.integration.sonarqube.ssc.metric.MetricValueTypeHelper;
import com.fortify.integration.sonarqube.ssc.metric.provider.AbstractFortifyMetricProvider;
import com.fortify.integration.sonarqube.ssc.metric.provider.IFortifyMetricProvider;
import com.fortify.integration.sonarqube.ssc.metric.provider.IFortifyMetricsProvider;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.spring.SpringExpressionUtil;


/**
 * This {@link IFortifyMetricsProvider} implementation provides {@link Metric} definitions
 * and values for various Fortify performance indicator and variable values. 
 * 
 * @author Ruud Senden
 *
 */
public final class FortifySnapshotMetricsProvider implements IFortifyMetricsProvider {
	private static final String DOMAIN = "Fortify - Metrics";
	private static final IFortifyMetricProvider[] METRIC_PROVIDERS = {
		new FortifySnapshotMetricValueRetriever(new Metric.Builder("fortify.ssc.securityRating",
				"Fortify Security Rating (SSC)", Metric.ValueType.FLOAT).setDescription("Fortify Security Rating (SSC)")
				.setDirection(Metric.DIRECTION_BETTER).setQualitative(true).setDomain(DOMAIN)
				.create(), "pi['Fortify Securit yRating']"),
		
		new FortifySnapshotMetricValueRetriever(new Metric.Builder("fortify.ssc.remediationEffort",
				"Total Remediation Effort (SSC)", Metric.ValueType.FLOAT).setDescription("Total Remediation Effort (SSC)")
				.setDirection(Metric.DIRECTION_BETTER).setQualitative(true).setDomain(DOMAIN)
				.create(), "pi['Remediation Effort Total']"),
		
		new FortifySnapshotMetricValueRetriever(new Metric.Builder("fortify.ssc.ACFPO", "Audited Critical Priority Issues (SSC)",
				Metric.ValueType.PERCENT).setDescription("Audited Critical Issues (SSC)").setDirection(Metric.DIRECTION_BETTER)
				.setQualitative(true).setDomain(DOMAIN).create(), "pi['Critical Priority Issues Audited']"),
		
		new FortifySnapshotMetricValueRetriever(new Metric.Builder("fortify.ssc.AHFPO", "Audited High Priority Issues (SSC)",
				Metric.ValueType.PERCENT).setDescription("Audited High Priority Issues (SSC)")
				.setDirection(Metric.DIRECTION_BETTER).setQualitative(true).setDomain(DOMAIN)
				.create(), "pi['High Priority Issues Audited']"),
		
		new FortifySnapshotMetricValueRetriever(new Metric.Builder("fortify.ssc.CFPO", "Critical Priority Issues (SSC)",
				Metric.ValueType.INT).setDescription("Critical Issues (SSC)").setDirection(Metric.DIRECTION_WORST)
				.setQualitative(true).setDomain(DOMAIN).create(), "var['CFPO']"),
		
		new FortifySnapshotMetricValueRetriever(new Metric.Builder("fortify.ssc.HFPO", "High Priority Issues (SSC)",
				Metric.ValueType.INT).setDescription("High Priority Issues (SSC)").setDirection(Metric.DIRECTION_WORST)
				.setQualitative(true).setDomain(DOMAIN).create(), "var['HFPO']"),
		
		new FortifySnapshotMetricValueRetriever(new Metric.Builder("fortify.ssc.MFPO", "Medium Priority Issues (SSC)",
				Metric.ValueType.INT).setDescription("Medium Priority Issues (SSC)").setDirection(Metric.DIRECTION_WORST)
				.setQualitative(true).setDomain(DOMAIN).create(), "var['MFPO']"),
		
		new FortifySnapshotMetricValueRetriever(new Metric.Builder("fortify.ssc.LFPO", "Low Priority Issues (SSC)",
				Metric.ValueType.INT).setDescription("Low Priority Issues (SSC)").setDirection(Metric.DIRECTION_WORST)
				.setQualitative(true).setDomain(DOMAIN).create(), "var['LFPO']"),
		
		new FortifySnapshotMetricValueRetriever(new Metric.Builder("fortify.ssc.LOC", "Lines of Code (SSC)",
				Metric.ValueType.INT).setDescription("Lines of Code (SSC)").setDirection(Metric.DIRECTION_NONE)
				.setQualitative(false).setDomain(DOMAIN).create(), "var['LOC']"),
		
		new FortifySnapshotMetricValueRetriever(new Metric.Builder("fortify.ssc.ExecutableLOC", "Executable Lines of Code (SSC)",
				Metric.ValueType.INT).setDescription("Executable Lines of Code (SSC)").setDirection(Metric.DIRECTION_NONE)
				.setQualitative(false).setDomain(DOMAIN).create(), "var['ExecutableLOC']"),
		
		new FortifySnapshotMetricValueRetriever(new Metric.Builder("fortify.ssc.FILES", "Number of Files (SSC)",
				Metric.ValueType.INT).setDescription("Number of Files (SSC)").setDirection(Metric.DIRECTION_NONE)
				.setQualitative(false).setDomain(DOMAIN).create(), "var['FILES']")
	};
	
	@Override
	public Collection<IFortifyMetricProvider> getMetricProviders() {
		return Arrays.asList(METRIC_PROVIDERS);
	}
	
	private static final class FortifySnapshotMetricValueRetriever extends AbstractFortifyMetricProvider {
		private final String expression;
		public FortifySnapshotMetricValueRetriever(Metric<Serializable> metric, String expression) {
			super(metric);
			this.expression = expression;
		}
		@Override
		public Serializable getValue(SensorContext context, FortifySSCConnectionFactory connFactory) {
			JSONMap metrics = new JSONMap();
			metrics.put("var", connFactory.getApplicationVersion().get(MetricType.variable.toString(), JSONList.class).toJSONMap("name", String.class, "value", Object.class));
			metrics.put("pi", connFactory.getApplicationVersion().get(MetricType.performanceIndicator.toString(), JSONList.class).toJSONMap("name", String.class, "value", Object.class));
			return (Serializable) SpringExpressionUtil.evaluateExpression(metrics, expression, MetricValueTypeHelper.getValueTypeClass(getMetric()));
		}
	}
}
