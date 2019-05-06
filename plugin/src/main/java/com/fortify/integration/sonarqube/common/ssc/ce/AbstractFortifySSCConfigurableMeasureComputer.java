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
package com.fortify.integration.sonarqube.common.ssc.ce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.sonar.api.ce.measure.Component;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.client.ssc.annotation.SSCCopyToConstructors;
import com.fortify.client.ssc.api.SSCApplicationVersionAPI;
import com.fortify.client.ssc.api.SSCArtifactAPI;
import com.fortify.client.ssc.api.query.builder.SSCOrderByDirection;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.integration.sonarqube.common.SourceSystem;
import com.fortify.integration.sonarqube.common.config.MetricsConfig;
import com.fortify.integration.sonarqube.common.config.MetricsConfig.MetricConfig;
import com.fortify.integration.sonarqube.common.ssc.SSCMetricsExpressionField;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.ondemand.AbstractJSONMapOnDemandLoaderWithConnection;
import com.fortify.util.rest.json.preprocessor.enrich.JSONMapEnrichWithOnDemandJSONMapFromJSONList;
import com.fortify.util.rest.json.preprocessor.enrich.JSONMapEnrichWithOnDemandProperty;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter.MatchMode;
import com.fortify.util.rest.json.preprocessor.filter.JSONMapFilterSpEL;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This {@link AbstractFortifySSCMeasureComputerWithConnectionHelper} implementation provides
 * the following functionality:
 * <ul>
 *   <li>Load configurable metric definitions using {@link MetricsConfig}</li>
 *   <li>Defines the corresponding SonarQube metrics using the {@link MetricsImpl} class</li>
 *   <li>Load the application version data used as input for the metric value calculations</li>
 *   <li>Calculates the metric value for each configured metric definition, and reports this
 *       as a SonarQube measure</li> 
 * </ul>
 * 
 * Concrete version-specific implementations must provide an implementation for the 
 * {@link #getComputeEngineSideConnectionHelper(org.sonar.api.ce.measure.MeasureComputer.MeasureComputerContext)}
 * method to return a version-specific {@link IFortifySSCComputeEngineSideConnectionHelper}
 * instance.
 * 
 * TODO Describe available on-demand properties
 * TODO Add additional on-demand properties, for example issue group counts
 * 
 * @author Ruud Senden
 *
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractFortifySSCConfigurableMeasureComputer extends AbstractFortifySSCMeasureComputerWithConnectionHelper {
	private static final Logger LOG = Loggers.get(AbstractFortifySSCConfigurableMeasureComputer.class);
	private static final MetricsConfig METRICS_CONFIG = MetricsConfig.load(SourceSystem.SSC);
	private static final List<Metric> METRICS = _getMetrics();
	
	/**
	 * Define the metric keys for which SonarQube measures are calculated by this
	 * measure computer, based on the configurable metrics provided by {@link MetricsConfig}.
	 */
	@Override
	protected String[] getOutputMetricKeys() {
		return METRICS.stream().map(Metric::getKey).collect(Collectors.toList()).toArray(new String[] {});
	}
	
	/**
	 * This method returns {@link Component.Type.PROJECT} to indicate that this
	 * measure computer should only run at project level.
	 */
	@Override
	protected Set<Component.Type> getSupportedComponentTypes() {
		return Collections.singleton(Component.Type.PROJECT);
	}
	
	/**
	 * This method simply gets the SSC applition version data using
	 * {@link #getApplicationVersionData(IFortifySSCComputeEngineSideConnectionHelper)},
	 * and then calls {@link #addMeasures(org.sonar.api.ce.measure.MeasureComputer.MeasureComputerContext, JSONMap)}
	 * to calculate the measures.
	 */
	@Override
	public void compute(MeasureComputerContext context, IFortifySSCComputeEngineSideConnectionHelper connHelper) {
		addMeasures(context, getApplicationVersionData(connHelper));
	}

	/**
	 * For every configurable metric defined through {@link MetricsConfig}, this method
	 * evaluates the corresponding metric expression and adds the expression return
	 * value as a SonarQube measure.
	 *  
	 * @param context
	 * @param applicationVersion
	 */
	private void addMeasures(MeasureComputerContext context, JSONMap applicationVersion) {
		for ( MetricConfig mc : METRICS_CONFIG.getMetrics() ) {
			// TODO Any better way for implementing this?
			try {
				Object value = SpringExpressionUtil.evaluateExpression(applicationVersion, mc.getExpr(), mc.getType().valueType());
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
	private JSONMap getApplicationVersionData(IFortifySSCComputeEngineSideConnectionHelper connHelper) {
		SSCAuthenticatingRestConnection conn = connHelper.getConnection();
		String applicationVersionId = connHelper.getApplicationVersionId();
		JSONMap applicationVersion = connHelper.getConnection().api(SSCApplicationVersionAPI.class).queryApplicationVersions()
				.id(applicationVersionId)
				.onDemandFilterSets(SSCMetricsExpressionField.filterSets.name())
				.onDemandPerformanceIndicatorHistories(SSCMetricsExpressionField.performanceIndicatorHistories.name())
				.onDemandVariableHistories(SSCMetricsExpressionField.variableHistories.name())
				// Add convenience properties for defining custom metrics
				.preProcessor(new JSONMapEnrichWithOnDemandJSONMapFromJSONList(SSCMetricsExpressionField.var.name(), SSCMetricsExpressionField.variableHistories.name(), "name", "value", true))
				.preProcessor(new JSONMapEnrichWithOnDemandJSONMapFromJSONList(SSCMetricsExpressionField.pi.name(), SSCMetricsExpressionField.performanceIndicatorHistories.name(), "name", "value", true))
				.preProcessor(new JSONMapEnrichWithOnDemandProperty(SSCMetricsExpressionField.scaArtifact.name(), new JSONMapOnDemandLoaderMostRecentSuccessfulSCAOrNotCompletedArtifact(conn)))
				.build().getUnique();
		if ( applicationVersion==null ) {
			throw new IllegalArgumentException("SSC application version "+applicationVersionId+" not found");
		}
		return applicationVersion;
	}
	
	/**
	 * This method generates a list of {@link Metric} instances based on the
	 * configurable metrics provided through {@link MetricsConfig}.
	 * @return
	 */
	private static final List<Metric> _getMetrics() {
		List<Metric> result = new ArrayList<>();
		for ( MetricConfig mc : METRICS_CONFIG.getMetrics() ) {
			Metric.ValueType type = Metric.ValueType.valueOf(mc.getType().name());
			result.add(new Metric.Builder(mc.getKey(), mc.getName(), type)
					.setDescription(mc.getDescription()).setDirection(mc.getDirection().intValue())
					.setQualitative(mc.isQualitative()).setDomain(mc.getDomain()).create());
		}
		return result;
	}
	
	/**
	 * This {@link Metrics} implementation simply returns the cached
	 * output of the {@link AbstractFortifySSCConfigurableMeasureComputer#_getMetrics()}
	 * method.
	 * 
	 * @author Ruud Senden
	 *
	 */
	public static final class MetricsImpl implements Metrics {
		
		@Override
		public List<Metric> getMetrics() {
			return METRICS;
		}
	}
	
	/**
	 * This {@link AbstractJSONMapOnDemandLoaderWithConnection} implementation provides
	 * either the most recently processed SCA artifact, or the most recent artifact that
	 * has not yet been successfully processed (which may be an SCA or other artifact; we
	 * can't tell until SSC has processed the artifact), as an on-demand property.
	 *   
	 * @author Ruud Senden
	 *
	 */
	private static final class JSONMapOnDemandLoaderMostRecentSuccessfulSCAOrNotCompletedArtifact extends AbstractJSONMapOnDemandLoaderWithConnection<SSCAuthenticatingRestConnection> {
		private static final long serialVersionUID = 1L;

		public JSONMapOnDemandLoaderMostRecentSuccessfulSCAOrNotCompletedArtifact(SSCAuthenticatingRestConnection conn) {
			super(conn, true);
		}
		
		@Override @SSCCopyToConstructors
		public Object getOnDemand(SSCAuthenticatingRestConnection conn, String propertyName, JSONMap parent) {
			return conn.api(SSCArtifactAPI.class).queryArtifacts(parent.get("id", String.class))
					.paramOrderBy("uploadDate", SSCOrderByDirection.DESC)
					.paramEmbedScans()
					.preProcessor(new JSONMapFilterSpEL(MatchMode.INCLUDE, "(_embed.scans?.get(0)?.type=='SCA' && status=='PROCESS_COMPLETE') || status matches 'PROCESSING|SCHED_PROCESSING|REQUIRE_AUTH|ERROR_PROCESSING'"))
					.useCache(false).maxResults(1).build().getUnique();
		}
		
		@Override
		protected Class<SSCAuthenticatingRestConnection> getConnectionClazz() {
			return SSCAuthenticatingRestConnection.class;
		}
	}

}
