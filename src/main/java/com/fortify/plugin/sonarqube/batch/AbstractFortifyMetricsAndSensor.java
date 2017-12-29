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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.plugin.sonarqube.FortifySSCConnectionFactory;

public abstract class AbstractFortifyMetricsAndSensor implements Metrics, Sensor {
	private static final Logger LOG = Loggers.get(AbstractFortifyMetricsAndSensor.class); 
	private final FortifySSCConnectionFactory connFactory;
	private final Map<Metric<Serializable>, MetricValueRetriever> metricsMap = new LinkedHashMap<>();
	
	/**
	 * Constructor for injecting {@link FortifySSCConnectionFactory}
	 * @param connFactory
	 */
	public AbstractFortifyMetricsAndSensor(FortifySSCConnectionFactory connFactory) {
		this.connFactory = connFactory;
		addMetrics(metricsMap);
	}
	
	public FortifySSCConnectionFactory getConnFactory() {
		return connFactory;
	}

	/**
	 * Get the metrics provided by {@link #addMetrics(Map)}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public List<Metric> getMetrics() {
		List<Metric> result = new ArrayList<Metric>();
		result.addAll(metricsMap.keySet());
		return result;
	}
	
	/**
	 * Describe this sensor
	 */
	@Override
	public void describe(SensorDescriptor descriptor) {
		descriptor.name(this.toString());
	}

	/**
	 * Generate all metric values by iterating over all entries in the metrics map
	 * returned by the configured {@link FortifyMetricsMapProvider}, invoking the
	 * {@link MetricValueRetriever} instance associated with each {@link Metric} 
	 * instance to actually generate the metric value.
	 */
	@Override
	public final void execute(SensorContext context) {
		try {
			executeBeforeMetricsCalculation(context);
			if ( isActive(context) ) {
				for ( Map.Entry<Metric<Serializable>, MetricValueRetriever> entry : metricsMap.entrySet() ) {
					Metric<Serializable> metric = entry.getKey();
					MetricValueRetriever valueRetriever = entry.getValue();
					if ( valueRetriever.isActive(context) ) {
						Serializable value = valueRetriever.getMetricValue(context, metric, getConnFactory());
						if ( value == null ) {
							LOG.debug("Not adding null value for metric "+metric.getKey());
						} else {
							LOG.debug("Adding metric "+metric.getKey()+" with value "+value);
							context.newMeasure().forMetric(metric).on(context.module()).withValue(value).save();
						}
					}
				}
			}
			executeAfterMetricsCalculation(context);
		} catch ( Exception e ) {
			LOG.error("Exception occured during Fortify sensor execution", e);
		}
	}
	
	protected void executeBeforeMetricsCalculation(SensorContext context) {}
	protected void executeAfterMetricsCalculation(SensorContext context) {}
	
	/**
	 * @param context
	 * @return true if SSC connection is available, false otherwise
	 */
	private final boolean isActive(SensorContext context) {
		return getConnFactory().isConnectionAvailable();
	}
	
	/**
	 * This implementation simply returns the class name
	 */
	@Override
	public String toString() {
		return this.getClass().getName();
	}
	
	protected abstract void addMetrics(Map<Metric<Serializable>, MetricValueRetriever> metricsMap);
	
	/**
	 * Interface for retrieving a metric value for the given {@link Metric}.
	 */
	public static interface MetricValueRetriever {
		public Serializable getMetricValue(SensorContext context, Metric<Serializable> metric, FortifySSCConnectionFactory connFactory);
		public boolean isActive(SensorContext context);
	}
	
	/**
	 * Abstract implementation for {@link MetricValueRetriever} that defines some default behavior.
	 */
	public static abstract class AbstractMetricValueRetriever implements MetricValueRetriever {
		@Override
		public boolean isActive(SensorContext context) {
			return true;
		}
	}
}
