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
package com.fortify.integration.sonarqube.common.ce;

import java.util.Set;
import java.util.stream.Stream;

import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * This abstract {@link MeasureComputer} base class provides the following functionality:
 * <ul>
 *   <li>Define input metrics, combining the metrics from {@link #getConnectionPropertiesMetricKeys()}
 *       and any additional input metrics returned by the {@link #getInputMetricKeys()} method.</li>
 *   <li>Define output metrics based on the {@link #getOutputMetricKeys()} method.</li>
 *   <li>Call the abstract {@link #getComputeEngineSideConnectionHelper(org.sonar.api.ce.measure.MeasureComputer.MeasureComputerContext)}
 *       method to get a version-specific {@link IFortifyComputeEngineSideConnectionHelper} instance.</li>
 *   <li>Call the abstract {@link #compute(org.sonar.api.ce.measure.MeasureComputer.MeasureComputerContext, IFortifyComputeEngineSideConnectionHelper)}
 *       method if the current component type is supported (as defined by the {@link #getSupportedComponentTypes()} method),
 *       and if a connection is available.
 * </ul>
 * @author Ruud Senden
 *
 */
public abstract class AbstractFortifyMeasureComputerWithConnectionHelper<CH extends IFortifyComputeEngineSideConnectionHelper<?>> implements MeasureComputer {
	private static final Logger LOG = Loggers.get(AbstractFortifyMeasureComputerWithConnectionHelper.class);
	private static final String[] EMPTY_STRING_ARRAY = new String[] {};
	
	/**
	 * Define this {@link MeasureComputer}, combining the metrics from {@link #getConnectionPropertiesMetricKeys()}
	 * and the {@link #getInputMetricKeys()} to define the input metrics, and
	 * the metric keys returned by {@link #getOutputMetricKeys()} as the output
	 * metrics.
	 */
	@Override
	public final MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
		return defContext.newDefinitionBuilder()
			.setInputMetrics(Stream.of(getConnectionPropertiesMetricKeys(), getInputMetricKeys()).flatMap(Stream::of).toArray(String[]::new))
			.setOutputMetrics(getOutputMetricKeys())
			.build();
	}
	
	/**
	 * This method checks whether the current {@link Component} is supported by
	 * the concrete implementation (based on the {@link #getSupportedComponentTypes()} 
	 * return value), and whether the connection to SSC is available. If so, the
	 * {@link #compute(org.sonar.api.ce.measure.MeasureComputer.MeasureComputerContext, IFortifyComputeEngineSideConnectionHelper)}
	 * method is called to allow the concrete implementation to compute the measures.
	 */
	@Override
	public final void compute(MeasureComputerContext context) {
		Set<Component.Type> supportedComponentTypes = getSupportedComponentTypes();
		if ( supportedComponentTypes==null || supportedComponentTypes.contains(context.getComponent().getType()) ) {
			CH connHelper = getComputeEngineSideConnectionHelper(context);
			if ( !connHelper.isConnectionAvailable() ) {
				LOG.info("Skipping measure computation; SSC connection has not been configured");
			} else {
				compute(context, connHelper);
			}
		}
	}
	
	/**
	 * Version-specific implementations must return a version-specific {@link IFortifyComputeEngineSideConnectionHelper}
	 * instance. Implementations should never return null.
	 * 
	 * @param context
	 * @return
	 */
	protected abstract CH getComputeEngineSideConnectionHelper(MeasureComputerContext context);

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
	 * Subclasses need to implement this method to return the input metric keys
	 * for metrics containing the connection properties.
	 * @return
	 */
	protected abstract String[] getConnectionPropertiesMetricKeys();
	
	/**
	 * Subclasses need to implement this method to return the output metric keys
	 * @return
	 */
	protected abstract String[] getOutputMetricKeys();
	
	/**
	 * Subclasses can override this method to return the component types for which to
	 * invoke the {@link #compute(org.sonar.api.ce.measure.MeasureComputer.MeasureComputerContext, FortifySSCSQ67ComputeEngineSideConnectionHelper)}
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
	protected abstract void compute(MeasureComputerContext context, CH connHelper);
}
