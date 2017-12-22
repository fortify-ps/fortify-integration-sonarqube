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
package com.fortify.plugin.sonarqube.batch;

import java.io.Serializable;
import java.util.Map;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.Metric;

import com.fortify.api.ssc.connection.api.query.builder.SSCOrderByDirection;
import com.fortify.api.util.rest.json.JSONMap;
import com.fortify.api.util.rest.json.preprocessor.AbstractJSONMapFilter.MatchMode;
import com.fortify.plugin.sonarqube.FortifySSCConnectionFactory;
import com.fortify.api.util.rest.json.preprocessor.JSONMapFilterSpEL;

/**
 * This {@link FortifyMetricsProvider} implementation provides {@link Metric} definitions
 * and corresponding {@link MetricValueRetriever} implementations for Fortify artifact
 * data like scan date, upload date and processing status.
 * 
 * @author Ruud Senden
 *
 */
public final class FortifyArtifactMetricsAndSensor extends AbstractFortifyMetricsAndSensor {
	public FortifyArtifactMetricsAndSensor(FortifySSCConnectionFactory connFactory) {
		super(connFactory);
	}

	@Override
	public void addMetrics(Map<Metric<Serializable>, MetricValueRetriever> metricsMap) {
		metricsMap.put(new Metric.Builder("fortify.artifact.approvalDate", "Approval Date",
				Metric.ValueType.STRING).setDescription("Artifact approval date").setDirection(Metric.DIRECTION_NONE)
						.setQualitative(false).setDomain("Fortify - Artifact").create(),
				new AbstractMetricValueRetriever() {
					@Override
					public Serializable getMetricValue(SensorContext context, Metric<Serializable> metric, FortifySSCConnectionFactory connFactory) {
						return getMemoizedMostRecentSuccessfulSCAOrNotCompletedArtifact(connFactory).get("approvalDate", String.class);
					}
				});
		metricsMap.put(new Metric.Builder("fortify.artifact.lastScanDate", "Last Scan Date",
				Metric.ValueType.STRING).setDescription("Artifact last scan date").setDirection(Metric.DIRECTION_NONE)
						.setQualitative(false).setDomain("Fortify - Artifact").create(),
				new AbstractMetricValueRetriever() {
					@Override
					public Serializable getMetricValue(SensorContext context, Metric<Serializable> metric, FortifySSCConnectionFactory connFactory) {
						return getMemoizedMostRecentSuccessfulSCAOrNotCompletedArtifact(connFactory).get("lastScanDate", String.class);
					}
				});
		metricsMap.put(new Metric.Builder("fortify.artifact.originalFileName", "Scan File Name",
				Metric.ValueType.STRING).setDescription("Artifact scan file name").setDirection(Metric.DIRECTION_NONE)
						.setQualitative(false).setDomain("Fortify - Artifact").create(),
				new AbstractMetricValueRetriever() {
					@Override
					public Serializable getMetricValue(SensorContext context, Metric<Serializable> metric, FortifySSCConnectionFactory connFactory) {
						return getMemoizedMostRecentSuccessfulSCAOrNotCompletedArtifact(connFactory).get("originalFileName", String.class);
					}
				});
		metricsMap.put(new Metric.Builder("fortify.artifact.uploadDate", "Scan Upload Date",
				Metric.ValueType.STRING).setDescription("Artifact scan upload date").setDirection(Metric.DIRECTION_NONE)
						.setQualitative(false).setDomain("Fortify - Artifact").create(),
				new AbstractMetricValueRetriever() {
					@Override
					public Serializable getMetricValue(SensorContext context, Metric<Serializable> metric, FortifySSCConnectionFactory connFactory) {
						return getMemoizedMostRecentSuccessfulSCAOrNotCompletedArtifact(connFactory).get("uploadDate", String.class);
					}
				});
		metricsMap.put(new Metric.Builder("fortify.artifact.messages", "Artifact messages",
				Metric.ValueType.STRING).setDescription("Artifact messages").setDirection(Metric.DIRECTION_NONE)
						.setQualitative(false).setDomain("Fortify - Artifact").create(),
				new AbstractMetricValueRetriever() {
					@Override
					public Serializable getMetricValue(SensorContext context, Metric<Serializable> metric, FortifySSCConnectionFactory connFactory) {
						return getMemoizedMostRecentSuccessfulSCAOrNotCompletedArtifact(connFactory).get("messages", String.class);
					}
				});
		metricsMap.put(new Metric.Builder("fortify.artifact.status", "Artifact Status",
				Metric.ValueType.STRING).setDescription("Artifact status").setDirection(Metric.DIRECTION_NONE)
						.setQualitative(false).setDomain("Fortify - Artifact").create(),
				new AbstractMetricValueRetriever() {
					@Override
					public Serializable getMetricValue(SensorContext context, Metric<Serializable> metric, FortifySSCConnectionFactory connFactory) {
						return getMemoizedMostRecentSuccessfulSCAOrNotCompletedArtifact(connFactory).get("status", String.class);
					}
				});
	}
	
	private static final JSONMap getMemoizedMostRecentSuccessfulSCAOrNotCompletedArtifact(FortifySSCConnectionFactory connFactory) {
		if ( connFactory.getArtifact() != null ) {
			return connFactory.getArtifact();
		} else {
			return connFactory.getConnectionWithArtifactProcessing().api().artifact().queryArtifacts(connFactory.getApplicationVersionId())
				.paramOrderBy("uploadDate", SSCOrderByDirection.DESC)
				.preProcessor(new JSONMapFilterSpEL(MatchMode.INCLUDE, "(_embed.scans?.getJSONObject(0)?.type=='SCA' && status=='PROCESS_COMPLETE') || status matches 'PROCESSING|SCHED_PROCESSING|REQUIRE_AUTH|ERROR_PROCESSING'"))
				.useCache(true).maxResults(1).build().getUnique();
		}
	}
}
