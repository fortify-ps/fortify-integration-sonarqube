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
package com.fortify.integration.sonarqube.common.metrics;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import com.fortify.integration.sonarqube.common.config.MetricsConfig;
import com.fortify.integration.sonarqube.common.config.MetricsConfig.MetricConfig;

/**
 * This {@link Metrics} implementation returns the {@link Metric} instances
 * corresponding to the {@link MetricsConfig} provided in the constructor.
 * 
 * @author Ruud Senden
 *
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractFortifyConfigurableMetrics implements Metrics {
	private List<Metric> metrics;
	
	protected AbstractFortifyConfigurableMetrics(MetricsConfig metricsConfig) {
		this.metrics = _getMetrics(metricsConfig);
	}
	
	protected static final List<Metric> _getMetrics(MetricsConfig metricsConfig) {
		List<Metric> result = new ArrayList<>();
		for ( MetricConfig mc : metricsConfig.getMetrics() ) {
			Metric.ValueType type = Metric.ValueType.valueOf(mc.getType().name());
			result.add(new Metric.Builder(mc.getKey(), mc.getName(), type)
					.setDescription(mc.getDescription()).setDirection(mc.getDirection().intValue())
					.setQualitative(mc.isQualitative()).setDomain(mc.getDomain()).create());
		}
		return result;
	}
	
	@Override
	public List<Metric> getMetrics() {
		return metrics;
	}
}