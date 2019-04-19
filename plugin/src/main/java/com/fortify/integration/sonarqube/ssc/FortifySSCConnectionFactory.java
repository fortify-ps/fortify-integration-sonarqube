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
package com.fortify.integration.sonarqube.ssc;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.client.ssc.annotation.SSCCopyToConstructors;
import com.fortify.client.ssc.api.SSCApplicationVersionAPI;
import com.fortify.client.ssc.api.SSCArtifactAPI;
import com.fortify.client.ssc.api.query.builder.SSCOrderByDirection;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.ondemand.AbstractJSONMapOnDemandLoaderWithConnection;
import com.fortify.util.rest.json.preprocessor.enrich.JSONMapEnrichWithOnDemandJSONMapFromJSONList;
import com.fortify.util.rest.json.preprocessor.enrich.JSONMapEnrichWithOnDemandProperty;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter.MatchMode;
import com.fortify.util.rest.json.preprocessor.filter.JSONMapFilterSpEL;

/**
 * Connection factory used to access a {@link FortifySSCConnectionFactory} instance.
 * The connection instance is instantiated when this factory is instantiated, so
 * a single instance of this class will always return the same connection object.
 * 
 * @author Ruud Senden
 *
 */
@ScannerSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class FortifySSCConnectionFactory {
	/** SonarQube property holding the SSC URL */
	private static final String PRP_SSC_URL = "sonar.fortify.ssc.url";
	/** SonarQube property holding the SSC application version id or name */
	private static final String PRP_SSC_APP_VERSION = "sonar.fortify.ssc.appversion";
	/** SonarQube property holding the maximum number of seconds to wait for SSC to process the latest uploaded results */
	private static final String PRP_SSC_MAX_PROCESSING_TIMEOUT = "sonar.fortify.ssc.processing.timeout";
	/** SonarQube property specifying whether an exception should be thrown on the specified artifact states */
	private static final String PRP_SSC_FAIL_ON_ARTIFACT_STATES = "sonar.fortify.ssc.failOnArtifactStates";
	/** SonarQube property holding the FPR file name to upload */
	private static final String PRP_UPLOAD_FPR = "sonar.fortify.ssc.uploadFPR";
	
	
	private static final Logger LOG = Loggers.get(FortifySSCConnectionFactory.class);
	private static final int DEFAULT_PROCESSING_TIMEOUT = 120;
	private final Configuration config;
	private final SSCAuthenticatingRestConnection conn;
	private final JSONMap applicationVersion;
	private JSONMap artifact = null;
	private boolean isFirstCallToArtifactProcessing = true;
	
	/**
	 * Constructor that initializes the connection instance
	 * @param config
	 */
	public FortifySSCConnectionFactory(Configuration config) {
		this.config = config;
		
		// TODO According to SonarQube documentation, we shouldn't access configuration properties from constructor
		String url = config.get(PRP_SSC_URL).orElse(null);
		String applicationVersionNameOrId = config.get(PRP_SSC_APP_VERSION).orElse(null);
		if ( StringUtils.isBlank(url) || StringUtils.isBlank(applicationVersionNameOrId) ) {
			LOG.info("SSC connection information not available; not loading SSC results");
			this.conn = null;
			this.applicationVersion = null;
		} else {
			this.conn = SSCAuthenticatingRestConnection.builder().baseUrl(url).build();
			this.applicationVersion = conn.api(SSCApplicationVersionAPI.class).queryApplicationVersions()
					.nameOrId(applicationVersionNameOrId)
					.onDemandFilterSets()
					.onDemandPerformanceIndicatorHistories()
					.onDemandVariableHistories()
					// Add convenience properties for defining custom metrics
					.preProcessor(new JSONMapEnrichWithOnDemandJSONMapFromJSONList("var", "variableHistories", "name", "value", true))
					.preProcessor(new JSONMapEnrichWithOnDemandJSONMapFromJSONList("pi", "performanceIndicatorHistories", "name", "value", true))
					.preProcessor(new JSONMapEnrichWithOnDemandProperty("scaArtifact", new JSONMapOnDemandLoaderMostRecentSuccessfulSCAOrNotCompletedArtifact(conn)))
					.build().getUnique();
			if ( this.applicationVersion==null ) {
				throw new IllegalArgumentException("SSC application version "+applicationVersionNameOrId+" not found");
			}
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
					.useCache(true).maxResults(1).build().getUnique();
		}
		
		@Override
		protected Class<SSCAuthenticatingRestConnection> getConnectionClazz() {
			return SSCAuthenticatingRestConnection.class;
		}
	}
	
	/**
	 * @return {@link SSCAuthenticatingRestConnection} instance
	 */
	public final SSCAuthenticatingRestConnection getConnection() {
		if ( conn==null ) {
			throw new IllegalStateException("Connection to SSC not available");
		}
		return conn;
	}
	
	public final String getApplicationVersionId() {
		return applicationVersion.get("id", String.class);
	}
	
	public final JSONMap getApplicationVersion() {
		return applicationVersion;
	}
	
	public final JSONMap getArtifact() {
		return artifact;
	}
	
	/**
	 * Upon the first call to this method, we will optionally upload an FPR file, wait for 
	 * SSC to finish processing the most recent scan results (up to the configured time-out), 
	 * and depending on the failIfUploadNotSucessfullyProcessed setting check the artifact 
	 * status for successful processing completion. Subsequent calls will return the connection 
	 * immediately.
	 * @return {@link FortifySSCConnection} instance. 
	 */
	public final SSCAuthenticatingRestConnection getConnectionWithArtifactProcessing() {
		if ( isConnectionAvailable() && isFirstCallToArtifactProcessing ) {
			isFirstCallToArtifactProcessing  = false;
			String artifactId = uploadFPRAndWaitForProcessingToComplete();
			checkArtifactStatus(artifactId);
		}
		return getConnection();
	}
	
	private String uploadFPRAndWaitForProcessingToComplete() {
		String fprFileName = config.get(PRP_UPLOAD_FPR).orElse(null);
		int timeout = config.getInt(PRP_SSC_MAX_PROCESSING_TIMEOUT).orElse(DEFAULT_PROCESSING_TIMEOUT);
		if ( StringUtils.isNotBlank(fprFileName) ) {
			File file = new File(fprFileName);
			if ( file.exists() && file.isFile() && file.canRead() ) {
				LOG.info("Uploading FPR file "+file.getAbsolutePath());
				return getConnection().api(SSCArtifactAPI.class).uploadArtifactAndWaitProcessingCompletion(getApplicationVersionId(), file, timeout);
			} else {
				throw new IllegalArgumentException("FPR file doesn't exist or is not readable: "+fprFileName);
			}
		}
		return null;
	}

	private void checkArtifactStatus(String artifactId) {
		Set<String> failOnArtifactStates = new HashSet<>(Arrays.asList(config.getStringArray(PRP_SSC_FAIL_ON_ARTIFACT_STATES)));
		if ( !failOnArtifactStates.isEmpty() && artifactId!=null ) {
			this.artifact = getConnection().api(SSCArtifactAPI.class).getArtifactById(artifactId, false);
			String status = this.artifact.get("status", String.class);
			if ( failOnArtifactStates.contains(status) ) {
				throw new IllegalStateException("Failing on SSC artifact status "+status);
			}
		}
	}
	
	/**
	 * @return true if the SSC connection is available, false otherwise
	 */
	public final boolean isConnectionAvailable() {
		return conn != null && applicationVersion != null;
	}
	
	public static final void addPropertyDefinitions(List<PropertyDefinition> propertyDefinitions) {
		propertyDefinitions.add(PropertyDefinition.builder(PRP_SSC_URL)
				.name("SSC URL")
				.description("URL used to connect to SSC (http[s]://<user>:<password>@<host>[:port]/ssc or http[s]://authToken:token@<host>[:port]/ssc)")
				.type(PropertyType.PASSWORD)
				.build());
		/*
		propertyDefinitions.add(PropertyDefinition.builder(PRP_ENABLE_ISSUES)
				.name("Enable issues collection")
				.description("Enable collecting Fortify issues")
				.type(PropertyType.BOOLEAN)
				.defaultValue("true")
				.build());
		*/
		propertyDefinitions.add(PropertyDefinition.builder(PRP_SSC_MAX_PROCESSING_TIMEOUT)
				.name("Maximum processing time-out (seconds)")
				.description("Maximum amount of time SonarQube will wait for SSC to finish processing scan results")
				.type(PropertyType.INTEGER)
				.defaultValue("120")
				.build());
		propertyDefinitions.add(PropertyDefinition.builder(PRP_SSC_FAIL_ON_ARTIFACT_STATES)
				.name("Fail scan on artifact states")
				.description("Fail the SonarQube scan if the SSC artifact state matches one of these comma-separated values."+
						" Valid states are PROCESS_COMPLETE, REQUIRE_AUTH, ERROR_PROCESSING")
				.type(PropertyType.STRING)
				.defaultValue("")
				.build());
		
		// TODO Can we dynamically get the list of projects/versions from SSC?
		propertyDefinitions.add(PropertyDefinition.builder(PRP_SSC_APP_VERSION)
				.name("SSC Application Version")
				.description("SSC Application Version Id or Name (<application>:<version>).")
				.type(PropertyType.STRING)
				.onlyOnQualifiers(Qualifiers.PROJECT)
				.build());
		propertyDefinitions.add(PropertyDefinition.builder(PRP_UPLOAD_FPR)
				.name("FPR file to upload to SSC (optional)")
				.description("FPR file to upload to SSC")
				.type(PropertyType.STRING)
				.onlyOnQualifiers(Qualifiers.PROJECT)
				.build());
		/*
		propertyDefinitions.add(PropertyDefinition.builder(PRP_FILTER_SET)
				.name("Filter set id")
				.description("Filter set id used to retrieve issue data from SSC (optional)")
				.type(PropertyType.STRING)
				.onQualifiers(Qualifiers.PROJECT)
				.build());
		*/
	}
}
