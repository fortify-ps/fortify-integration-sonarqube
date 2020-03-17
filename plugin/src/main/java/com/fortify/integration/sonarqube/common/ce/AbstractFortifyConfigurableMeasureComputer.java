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
package com.fortify.integration.sonarqube.common.ce;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.sonar.api.ce.measure.Component;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.integration.sonarqube.common.config.MetricsConfig;
import com.fortify.integration.sonarqube.common.config.MetricsConfig.MetricConfig;
import com.fortify.integration.sonarqube.common.metrics.AbstractFortifyConfigurableMetrics;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This {@link AbstractFortifyMeasureComputerWithConnectionHelper} implementation provides
 * the following functionality:
 * <ul>
 *   <li>Load configurable metric definitions using {@link MetricsConfig}</li>
 *   <li>Defines the corresponding SonarQube metrics using the {@link AbstractFortifyConfigurableMetrics} class</li>
 *   <li>Load the application version data used as input for the metric value calculations</li>
 *   <li>Calculates the metric value for each configured metric definition, and reports this
 *       as a SonarQube measure</li> 
 * </ul>
 * 
 * Concrete version-specific implementations must provide an implementation for the 
 * {@link #getComputeEngineSideConnectionHelper(org.sonar.api.ce.measure.MeasureComputer.MeasureComputerContext)}
 * method to return a version-specific {@link IFortifyComputeEngineSideConnectionHelper}
 * instance.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractFortifyConfigurableMeasureComputer<CH extends IFortifyComputeEngineSideConnectionHelper<?>> extends AbstractFortifyMeasureComputerWithConnectionHelper<CH> {
	private static final Logger LOG = Loggers.get(AbstractFortifyConfigurableMeasureComputer.class);
	private final MetricsConfig metricsConfig;
	
	protected AbstractFortifyConfigurableMeasureComputer(MetricsConfig metricsConfig) {
		this.metricsConfig = metricsConfig;
	}
	
	/**
	 * Define the metric keys for which SonarQube measures are calculated by this
	 * measure computer, based on the configurable metrics provided by {@link MetricsConfig}.
	 */
	@Override
	protected final String[] getOutputMetricKeys() {
		return metricsConfig.getMetrics().stream().map(MetricConfig::getKey).collect(Collectors.toList()).toArray(new String[] {});
	}
	
	/**
	 * This method returns {@link Component.Type.PROJECT} to indicate that this
	 * measure computer should only run at project level.
	 */
	@Override
	protected final Set<Component.Type> getSupportedComponentTypes() {
		return Collections.singleton(Component.Type.PROJECT);
	}
	
	/**
	 * This method simply gets the SSC applition version data using
	 * {@link #getConfigurableMeasuresInputData(IFortifyComputeEngineSideConnectionHelper)},
	 * and then calls {@link #addMeasures(org.sonar.api.ce.measure.MeasureComputer.MeasureComputerContext, JSONMap)}
	 * to calculate the measures.
	 */
	@Override
	public final void compute(MeasureComputerContext context, CH connHelper) {
		addMeasures(context, getConfigurableMeasuresInputData(connHelper));
	}

	/**
	 * For every configurable metric defined through {@link MetricsConfig}, this method
	 * evaluates the corresponding metric expression and adds the expression return
	 * value as a SonarQube measure.
	 *  
	 * @param context
	 * @param applicationVersion
	 */
	private final void addMeasures(MeasureComputerContext context, JSONMap inputData) {
		for ( MetricConfig mc : metricsConfig.getMetrics() ) {
			// TODO Any better way for implementing this?
			try {
				Object value = SpringExpressionUtil.evaluateExpression(inputData, mc.getExpr(), mc.getType().valueType());
				if ( value != null ) {
					switch ( mc.getType() ) {
						case INT: case RATING:  
							context.addMeasure(mc.getKey(), (int)value);
							break;
						case MILLISEC: case WORK_DUR: 
							context.addMeasure(mc.getKey(), (long)value);
							break;
						case FLOAT: case PERCENT:
							context.addMeasure(mc.getKey(), (double)value);
							break;
						case BOOL:
							context.addMeasure(mc.getKey(), (boolean)value);
							break;
						default:
							context.addMeasure(mc.getKey(), (String)value);
							break;
					}
				}
			} catch ( RuntimeException e ) {
				LOG.error("Error computing metric "+mc.getKey()+" (expression: "+mc.getExpr()+")", e);
			}
		}
	}

	/**
	 * This method retrieves application version data from SSC. This data includes
	 * the standard application version JSON fields, as well as various on-demand
	 * fields that provide additional data that can be used in metric calculations.
	 *  
	 * @param connHelper
	 * @return
	 */
	protected abstract JSONMap getConfigurableMeasuresInputData(CH connHelper);
}
