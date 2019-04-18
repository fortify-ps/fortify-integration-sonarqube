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
package com.fortify.integration.sonarqube.ssc.metric.provider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.Metric;

import com.fortify.client.ssc.api.SSCMetricsAPI.MetricType;
import com.fortify.integration.sonarqube.ssc.FortifySSCConnectionFactory;
import com.fortify.integration.sonarqube.ssc.config.MetricsConfig;
import com.fortify.integration.sonarqube.ssc.config.MetricsConfig.MetricConfig;
import com.fortify.integration.sonarqube.ssc.metric.MetricValueTypeHelper;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

public class FortifyConfigurableMetricsProvider implements IFortifyMetricsProvider {
	private static final MetricsConfig metricsConfig = MetricsConfig.load();

	@Override
	public Collection<IFortifyMetricProvider> getMetricProviders() {
		List<IFortifyMetricProvider> result = new ArrayList<>();
		for ( Map.Entry<String, MetricConfig> entry : metricsConfig.getMetrics().entrySet() ) {
			String key = entry.getKey();
			MetricConfig mc = entry.getValue();
			Metric.ValueType type = Metric.ValueType.valueOf(mc.getType().name());
			result.add(new FortifySpELMetricValueRetriever(new Metric.Builder(key, mc.getName(), type)
					.setDescription(mc.getDescription()).setDirection(mc.getDirection().intValue())
					.setQualitative(mc.isQualitative()).setDomain(mc.getDomain()).create(), mc.getExpr()));
		}
		return result;
	}
	
	private static final class FortifySpELMetricValueRetriever extends AbstractFortifyMetricProvider {
		private final SimpleExpression expression;
		public FortifySpELMetricValueRetriever(Metric<Serializable> metric, String expression) {
			super(metric);
			this.expression = SpringExpressionUtil.parseSimpleExpression(expression);
		}
		@Override
		public Serializable getValue(SensorContext context, FortifySSCConnectionFactory connFactory) {
			JSONMap metrics = new JSONMap();
			// TODO, add properties here, or as on-demand properties in FortifySSCConnectionFactory? 
			metrics.put("var", connFactory.getApplicationVersion().get(MetricType.variable.toString(), JSONList.class).toJSONMap("name", String.class, "value", Object.class));
			metrics.put("pi", connFactory.getApplicationVersion().get(MetricType.performanceIndicator.toString(), JSONList.class).toJSONMap("name", String.class, "value", Object.class));
			return (Serializable) SpringExpressionUtil.evaluateExpression(metrics, expression, MetricValueTypeHelper.getValueTypeClass(getMetric()));
		}
	}

}
