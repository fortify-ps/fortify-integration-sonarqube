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
package com.fortify.integration.sonarqube.ssc.metric;

import java.io.Serializable;
import java.util.Arrays;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.measures.Metric;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.integration.sonarqube.ssc.FortifySSCConnectionFactory;
import com.fortify.integration.sonarqube.ssc.metric.provider.IFortifyMetricsProvider;

public class FortifyMetricsSensor implements Sensor {
	private static final Logger LOG = Loggers.get(FortifyMetricsSensor.class);
	private final IFortifyMetricsProvider[] metricsProviders;
	private final FortifySSCConnectionFactory connFactory;
	public FortifyMetricsSensor(IFortifyMetricsProvider[] metricsProviders, FortifySSCConnectionFactory connFactory) {
		this.metricsProviders = metricsProviders;
		this.connFactory = connFactory;
	}

	@Override
	public void describe(SensorDescriptor descriptor) {
		descriptor.name("Calculate Fortify metrics");
	}

	@Override
	public void execute(SensorContext context) {
		Arrays.asList(metricsProviders).forEach(mps -> mps.getMetricProviders().forEach(mp -> {
			Metric<Serializable> metric = mp.getMetric();
			Serializable value = mp.getValue(context, connFactory);
			if ( value == null ) {
				LOG.debug("Not adding null value for metric "+metric.getKey());
			} else {
				LOG.debug("Adding metric "+metric.getKey()+" with value "+value);
				context.newMeasure().forMetric(metric).on(context.module()).withValue(value).save();
			}
		}));
	}

}
