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
package com.fortify.integration.sonarqube.common.source.ssc.ce;

import com.fortify.client.ssc.annotation.SSCCopyToConstructors;
import com.fortify.client.ssc.api.SSCApplicationVersionAPI;
import com.fortify.client.ssc.api.SSCArtifactAPI;
import com.fortify.client.ssc.api.query.builder.SSCOrderByDirection;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.integration.sonarqube.common.ce.AbstractFortifyConfigurableMeasureComputer;
import com.fortify.integration.sonarqube.common.source.ssc.metrics.FortifySSCConfigurableMetrics;
import com.fortify.integration.sonarqube.common.ssc.SSCMetricsExpressionField;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.ondemand.AbstractJSONMapOnDemandLoaderWithConnection;
import com.fortify.util.rest.json.preprocessor.enrich.JSONMapEnrichWithOnDemandJSONMapFromJSONList;
import com.fortify.util.rest.json.preprocessor.enrich.JSONMapEnrichWithOnDemandProperty;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter.MatchMode;
import com.fortify.util.rest.json.preprocessor.filter.JSONMapFilterSpEL;

public final class FortifySSCConfigurableMeasureComputer extends AbstractFortifyConfigurableMeasureComputer<FortifySSCComputeEngineSideConnectionHelper> {
	public FortifySSCConfigurableMeasureComputer() {
		super(FortifySSCConfigurableMetrics.METRICS_CONFIG);
	}
	
	@Override
	protected final String[] getConnectionPropertiesMetricKeys() {
		return FortifySSCComputeEngineSideConnectionHelper.getInputMetricKeys();
	}
	
	@Override
	protected FortifySSCComputeEngineSideConnectionHelper getComputeEngineSideConnectionHelper(MeasureComputerContext context) {
		return new FortifySSCComputeEngineSideConnectionHelper(context);
	}
	
	/**
	 * This method retrieves application version data from SSC. This data includes
	 * the standard application version JSON fields, as well as various on-demand
	 * fields that provide additional data that can be used in metric calculations.
	 *  
	 * @param connHelper
	 * @return
	 */
	protected final JSONMap getConfigurableMeasuresInputData(FortifySSCComputeEngineSideConnectionHelper connHelper) {
		SSCAuthenticatingRestConnection conn = connHelper.getConnection();
		String applicationVersionId = connHelper.getApplicationVersionId();
		JSONMap applicationVersion = conn.api(SSCApplicationVersionAPI.class).queryApplicationVersions()
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
