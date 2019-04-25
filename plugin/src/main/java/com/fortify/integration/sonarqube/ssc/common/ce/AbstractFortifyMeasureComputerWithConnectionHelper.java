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
package com.fortify.integration.sonarqube.ssc.common.ce;

import java.util.Set;
import java.util.stream.Stream;

import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.MeasureComputer;

import com.fortify.integration.sonarqube.ssc.sq67.ce.FortifySQ67ComputeEngineSideConnectionHelper;

public abstract class AbstractFortifyMeasureComputerWithConnectionHelper implements MeasureComputer {
	private static final String[] EMPTY_STRING_ARRAY = new String[] {};
	
	@Override
	public final MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
		return defContext.newDefinitionBuilder()
			.setInputMetrics(Stream.of(FortifySQ67ComputeEngineSideConnectionHelper.getInputMetricKeys(), getInputMetricKeys()).flatMap(Stream::of).toArray(String[]::new))
			.setOutputMetrics(getOutputMetricKeys())
			.build();
	}
	
	@Override
	public final void compute(MeasureComputerContext context) {
		Set<Component.Type> supportedComponentTypes = getSupportedComponentTypes();
		if ( supportedComponentTypes==null || supportedComponentTypes.contains(context.getComponent().getType()) ) {
			IFortifyComputeEngineSideConnectionHelper connHelper = getComputeEngineSideConnectionHelper(context);
			if ( connHelper.isConnectionAvailable() ) {
				compute(context, connHelper);
			}
		}
	}
	
	protected abstract IFortifyComputeEngineSideConnectionHelper getComputeEngineSideConnectionHelper(MeasureComputerContext context);

	/**
	 * Subclasses can override this method if they use any additional input metrics.
	 * This default implementation returns an empty String array; implementations should
	 * never return null.
	 * @return
	 */
	protected String[] getInputMetricKeys() {
		return EMPTY_STRING_ARRAY;
	}
	
	/**
	 * Subclasses need to implement this method to return the output metric keys
	 * @return
	 */
	protected abstract String[] getOutputMetricKeys();
	
	/**
	 * Subclasses can override this method to return the component types for which to
	 * invoke the {@link #compute(org.sonar.api.ce.measure.MeasureComputer.MeasureComputerContext, FortifySQ67ComputeEngineSideConnectionHelper)}
	 * method. The default implementation returns null, meaning that the compute method
	 * will be invoked for any component type.
	 * @return
	 */
	protected Set<Component.Type> getSupportedComponentTypes() {
		return null;
	}

	/**
	 * Subclasses need to implement this method to perform the actual computation for
	 * the given {@link MeasureComputerContext}, using the given {@link IFortifyComputeEngineSideConnectionHelper}
	 * as needed.
	 * @param context
	 * @param connHelper
	 */
	protected abstract void compute(MeasureComputerContext context, IFortifyComputeEngineSideConnectionHelper connHelper);
}
