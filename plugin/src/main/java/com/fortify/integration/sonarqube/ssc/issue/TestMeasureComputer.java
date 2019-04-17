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
package com.fortify.integration.sonarqube.ssc.issue;

import org.sonar.api.ce.measure.Component.Type;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.ce.measure.Settings;
import org.sonar.api.config.Configuration;

import com.fortify.client.ssc.api.SSCMetricsAPI.MetricType;
import com.fortify.integration.sonarqube.ssc.FortifySSCConnectionFactory;
import com.fortify.util.rest.json.JSONList;

public class TestMeasureComputer implements MeasureComputer {
	private final FortifySSCConnectionFactory connFactory;

	public TestMeasureComputer(Configuration configuration, FortifySSCConnectionFactory connFactory) {
		this.connFactory = connFactory;
		System.out.println("applicationVersionNameOrId: "+configuration.get("sonar.fortify.ssc.appversion").orElse(null));
	}

	@Override
	  public MeasureComputerDefinition define(MeasureComputerDefinitionContext def) {
	    return def.newDefinitionBuilder()
	      .setOutputMetrics(TestMetrics.TEST_METRIC.key())
	      .build();
	  }

	  @Override
	  public void compute(MeasureComputerContext context) {
		  System.out.println("ComponentType: "+context.getComponent().getType());
		  if ( context.getComponent().getType() == Type.PROJECT ) {
			  Settings settings = context.getSettings();
			  System.out.println("applicationVersionNameOrId: "+settings.getString("sonar.fortify.ssc.appversion"));
			  System.out.println("connFactory: "+connFactory);
			  System.out.println("applicationVersion: "+connFactory.getApplicationVersion());
			  JSONList metrics = connFactory.getApplicationVersion().get(MetricType.variable.toString(), JSONList.class);
			  long value = metrics.mapValue("id", "LOC", "value", Long.class);
		      context.addMeasure(TestMetrics.TEST_METRIC.key(), value);
		  }
	  }

}
