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
package com.fortify.integration.sonarqube.ssc.metric.provider.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.Metric;

import com.fortify.client.ssc.api.SSCArtifactAPI;
import com.fortify.client.ssc.api.query.builder.SSCOrderByDirection;
import com.fortify.integration.sonarqube.ssc.FortifySSCConnectionFactory;
import com.fortify.integration.sonarqube.ssc.metric.MetricValueTypeHelper;
import com.fortify.integration.sonarqube.ssc.metric.provider.AbstractFortifyMetricProvider;
import com.fortify.integration.sonarqube.ssc.metric.provider.IFortifyMetricProvider;
import com.fortify.integration.sonarqube.ssc.metric.provider.IFortifyMetricsProvider;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter.MatchMode;
import com.fortify.util.rest.json.preprocessor.filter.JSONMapFilterSpEL;

/**
 * This {@link IFortifyMetricsProvider} implementation provides {@link Metric} definitions
 * and values for Fortify artifact data like scan date, upload date and processing status.
 * 
 * @author Ruud Senden
 *
 */
public final class FortifyArtifactMetricsProvider implements IFortifyMetricsProvider {
	private static final String DOMAIN = "Fortify - Artifact";
	private static final IFortifyMetricProvider[] METRIC_PROVIDERS = {
			new FortifyArtifactMetricValueRetriever(new Metric.Builder("fortify.artifact.approvalDate", "Approval Date",
					Metric.ValueType.STRING).setDescription("Artifact approval date").setDirection(Metric.DIRECTION_NONE)
							.setQualitative(false).setDomain(DOMAIN).create(), "approvalDate"),
						
			new FortifyArtifactMetricValueRetriever(new Metric.Builder("fortify.artifact.lastScanDate", "Last Scan Date",
					Metric.ValueType.STRING).setDescription("Artifact last scan date").setDirection(Metric.DIRECTION_NONE)
							.setQualitative(false).setDomain(DOMAIN).create(), "lastScanDate"),
			
			new FortifyArtifactMetricValueRetriever(new Metric.Builder("fortify.artifact.originalFileName", "Scan File Name",
					Metric.ValueType.STRING).setDescription("Artifact scan file name").setDirection(Metric.DIRECTION_NONE)
							.setQualitative(false).setDomain(DOMAIN).create(), "originalFileName"),
			
			new FortifyArtifactMetricValueRetriever(new Metric.Builder("fortify.artifact.uploadDate", "Scan Upload Date",
					Metric.ValueType.STRING).setDescription("Artifact scan upload date").setDirection(Metric.DIRECTION_NONE)
							.setQualitative(false).setDomain(DOMAIN).create(), "uploadDate"),
						
			new FortifyArtifactMetricValueRetriever(new Metric.Builder("fortify.artifact.messages", "Artifact messages",
					Metric.ValueType.STRING).setDescription("Artifact messages").setDirection(Metric.DIRECTION_NONE)
							.setQualitative(false).setDomain(DOMAIN).create(), "messages"),
						
			new FortifyArtifactMetricValueRetriever(new Metric.Builder("fortify.artifact.status", "Artifact Status",
					Metric.ValueType.STRING).setDescription("Artifact status").setDirection(Metric.DIRECTION_NONE)
							.setQualitative(false).setDomain(DOMAIN).create(), "status")
	};
	
	@Override
	public Collection<IFortifyMetricProvider> getMetricProviders() {
		return Arrays.asList(METRIC_PROVIDERS);
	}
	
	private static final class FortifyArtifactMetricValueRetriever extends AbstractFortifyMetricProvider {
		private final String fieldName;
		public FortifyArtifactMetricValueRetriever(Metric<Serializable> metric, String fieldName) {
			super(metric);
			this.fieldName = fieldName;
		}
		@Override
		public Serializable getValue(SensorContext context, FortifySSCConnectionFactory connFactory) {
			return (Serializable) getMemoizedMostRecentSuccessfulSCAOrNotCompletedArtifact(connFactory).get(fieldName, MetricValueTypeHelper.getValueTypeClass(getMetric()));
		}
		
		private static final JSONMap getMemoizedMostRecentSuccessfulSCAOrNotCompletedArtifact(FortifySSCConnectionFactory connFactory) {
			if ( connFactory.getArtifact() != null ) {
				return connFactory.getArtifact();
			} else {
				return connFactory.getConnectionWithArtifactProcessing().api(SSCArtifactAPI.class).queryArtifacts(connFactory.getApplicationVersionId())
					.paramOrderBy("uploadDate", SSCOrderByDirection.DESC)
					.paramEmbedScans()
					.preProcessor(new JSONMapFilterSpEL(MatchMode.INCLUDE, "(_embed.scans?.get(0)?.type=='SCA' && status=='PROCESS_COMPLETE') || status matches 'PROCESSING|SCHED_PROCESSING|REQUIRE_AUTH|ERROR_PROCESSING'"))
					.useCache(true).maxResults(1).build().getUnique();
			}
		}
	}
}
