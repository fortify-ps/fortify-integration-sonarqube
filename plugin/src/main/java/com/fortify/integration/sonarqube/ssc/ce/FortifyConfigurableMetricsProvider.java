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
package com.fortify.integration.sonarqube.ssc.ce;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import com.fortify.client.ssc.annotation.SSCCopyToConstructors;
import com.fortify.client.ssc.api.SSCApplicationVersionAPI;
import com.fortify.client.ssc.api.SSCArtifactAPI;
import com.fortify.client.ssc.api.query.builder.SSCOrderByDirection;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.integration.sonarqube.ssc.config.MetricsConfig;
import com.fortify.integration.sonarqube.ssc.config.MetricsConfig.MetricConfig;
import com.fortify.integration.sonarqube.ssc.scanner.FortifyConnectionPropertiesSensor;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.ondemand.AbstractJSONMapOnDemandLoaderWithConnection;
import com.fortify.util.rest.json.preprocessor.enrich.JSONMapEnrichWithOnDemandJSONMapFromJSONList;
import com.fortify.util.rest.json.preprocessor.enrich.JSONMapEnrichWithOnDemandProperty;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter.MatchMode;
import com.fortify.util.rest.json.preprocessor.filter.JSONMapFilterSpEL;
import com.fortify.util.spring.SpringExpressionUtil;

@SuppressWarnings("rawtypes")
public class FortifyConfigurableMetricsProvider implements MeasureComputer {
	private static final MetricsConfig METRICS_CONFIG = MetricsConfig.load();
	private static final List<Metric> METRICS = _getMetrics();
	
	@Override
	public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
		return defContext.newDefinitionBuilder()
			.setInputMetrics(FortifyConnectionPropertiesSensor.PRP_SSC_URL, FortifyConnectionPropertiesSensor.PRP_APP_VERSION_ID)
			.setOutputMetrics(METRICS.stream().map(Metric::getKey).collect(Collectors.toList()).toArray(new String[] {}))
			.build();
	}
	
	@Override
	public void compute(MeasureComputerContext context) {
		if ( context.getComponent().getType() == Component.Type.PROJECT ) {
			String sscUrl = context.getMeasure(FortifyConnectionPropertiesSensor.PRP_SSC_URL).getStringValue();
			String applicationVersionId = context.getMeasure(FortifyConnectionPropertiesSensor.PRP_APP_VERSION_ID).getStringValue();
			
			// TODO Remove these System.out statements
			System.out.println("SSC URL: "+sscUrl);
			System.out.println("SSC app version id: "+applicationVersionId);
			
			SSCAuthenticatingRestConnection conn = SSCAuthenticatingRestConnection.builder().baseUrl(sscUrl).build();
			JSONMap applicationVersion = conn.api(SSCApplicationVersionAPI.class).queryApplicationVersions()
					.id(applicationVersionId)
					.onDemandFilterSets()
					.onDemandPerformanceIndicatorHistories()
					.onDemandVariableHistories()
					// Add convenience properties for defining custom metrics
					.preProcessor(new JSONMapEnrichWithOnDemandJSONMapFromJSONList("var", "variableHistories", "name", "value", true))
					.preProcessor(new JSONMapEnrichWithOnDemandJSONMapFromJSONList("pi", "performanceIndicatorHistories", "name", "value", true))
					.preProcessor(new JSONMapEnrichWithOnDemandProperty("scaArtifact", new JSONMapOnDemandLoaderMostRecentSuccessfulSCAOrNotCompletedArtifact(conn)))
					.build().getUnique();
			if ( applicationVersion==null ) {
				throw new IllegalArgumentException("SSC application version "+applicationVersionId+" not found");
			}
			for ( MetricConfig mc : METRICS_CONFIG.getMetrics() ) {
				// TODO Any better way for implementing this?
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
			}
		}
	}
	
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
	
	public static final class MetricsImpl implements Metrics {
		
		@Override
		public List<Metric> getMetrics() {
			return METRICS;
		}
	}
	
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
