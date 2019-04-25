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
package com.fortify.integration.sonarqube.ssc.sq67.ce;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.ce.measure.MeasureComputer.MeasureComputerContext;

import com.fortify.integration.sonarqube.ssc.common.IFortifyConnectionHelper;
import com.fortify.integration.sonarqube.ssc.common.ce.AbstractFortifyComputeEngineSideConnectionHelper;

public class FortifySQ67ComputeEngineSideConnectionHelper extends AbstractFortifyComputeEngineSideConnectionHelper {
	public FortifySQ67ComputeEngineSideConnectionHelper(MeasureComputerContext measureComputerContext) {
		super(measureComputerContext);
	}
	
	public static final class SensorImpl implements Sensor {
		private final IFortifyConnectionHelper connHelper;
		
		public SensorImpl(IFortifyConnectionHelper connHelper) {
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
}
