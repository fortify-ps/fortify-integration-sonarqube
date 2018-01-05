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
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.config.Settings;
import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.client.ssc.api.SSCApplicationVersionAPI;
import com.fortify.client.ssc.api.SSCArtifactAPI;
import com.fortify.client.ssc.api.SSCMetricsAPI.MetricType;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.propertyaccessor.MapPropertyAccessor;

/**
 * Connection factory used to access a {@link FortifySSCConnectionFactory} instance.
 * The connection instance is instantiated when this factory is instantiated, so
 * a single instance of this class will always return the same connection object.
 * 
 * @author Ruud Senden
 *
 */
@BatchSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
@ServerSide
@ComputeEngineSide
public class FortifySSCConnectionFactory {
	private static final Logger LOG = Loggers.get(FortifySSCConnectionFactory.class);
	private static final int DEFAULT_PROCESSING_TIMEOUT = 120;
	private final Settings settings;
	private final SSCAuthenticatingRestConnection conn;
	private final JSONMap applicationVersion;
	private JSONMap artifact = null;
	private boolean isFirstCallToArtifactProcessing = true;
	
	// TODO For some reason SpringExpressionUtil doesn't automatically pick up MapPropertyAccessor
	static {
		SpringExpressionUtil.addPropertyAccessors(new MapPropertyAccessor());
	}
	
	/**
	 * Constructor that initializes the connection instance
	 * @param settings
	 */
	public FortifySSCConnectionFactory(Settings settings) {
		this.settings = settings;
		
		String url = settings.getString(FortifyConstants.PRP_SSC_URL);
		String applicationVersionNameOrId = settings.getString(FortifyConstants.PRP_SSC_APP_VERSION);
		if ( StringUtils.isBlank(url) || StringUtils.isBlank(applicationVersionNameOrId) ) {
			LOG.info("SSC connection information not available; not loading SSC results");
			this.conn = null;
			this.applicationVersion = null;
		} else {
			this.conn = SSCAuthenticatingRestConnection.builder().baseUrl(url).build();
			this.applicationVersion = conn.api(SSCApplicationVersionAPI.class).queryApplicationVersions()
					.nameOrId(applicationVersionNameOrId)
					.onDemandFilterSets()
					.onDemandPerformanceIndicatorHistories(MetricType.performanceIndicator.toString())
					.onDemandVariableHistories(MetricType.variable.toString())
					.build().getUnique();
			if ( this.applicationVersion==null ) {
				throw new IllegalArgumentException("SSC application version "+applicationVersionNameOrId+" not found");
			}
		}
		
	}
	
	/**
	 * @return {@link SSCAuthenticatingRestConnection} instance
	 */
	private final SSCAuthenticatingRestConnection getConnection() {
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
		String fprFileName = settings.getString(FortifyConstants.PRP_UPLOAD_FPR);
		int timeout = settings.getInt(FortifyConstants.PRP_SSC_MAX_PROCESSING_TIMEOUT);
		if ( timeout == 0 ) { timeout = DEFAULT_PROCESSING_TIMEOUT; }
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
		Set<String> failOnArtifactStates = new HashSet<>(Arrays.asList(settings.getStringArray(FortifyConstants.PRP_SSC_FAIL_ON_ARTIFACT_STATES)));
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
}
