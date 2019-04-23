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
package com.fortify.integration.sonarqube.ssc.scanner;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.PropertyType;
import org.sonar.api.Startable;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.client.ssc.api.SSCArtifactAPI;
import com.fortify.util.rest.json.JSONMap;

/**
 * This class is responsible for uploading the FPR file to SSC
 * 
 * @author Ruud Senden
 *
 */
@ScannerSide
public class FortifyUploadFPRStartable implements Startable {
	/** SonarQube property holding the maximum number of seconds to wait for SSC to process the latest uploaded results */
	private static final String PRP_SSC_MAX_PROCESSING_TIMEOUT = "sonar.fortify.ssc.processing.timeout";
	/** SonarQube property specifying whether an exception should be thrown on the specified artifact states */
	private static final String PRP_SSC_FAIL_ON_ARTIFACT_STATES = "sonar.fortify.ssc.failOnArtifactStates";
	/** SonarQube property holding the FPR file name to upload */
	private static final String PRP_UPLOAD_FPR = "sonar.fortify.ssc.uploadFPR";
	
	private static final Logger LOG = Loggers.get(FortifyUploadFPRStartable.class);
	private static final int DEFAULT_PROCESSING_TIMEOUT = 120;
	private final Configuration config;
	private final FortifySSCScannerSideConnectionHelper connHelper;
	
	/**
	 * Constructor that initializes the connection instance
	 * @param config
	 */
	public FortifyUploadFPRStartable(Configuration config, FortifySSCScannerSideConnectionHelper connHelper) {
		this.config = config;
		this.connHelper = connHelper;
	}
	
	@Override
	public void start() {
		String fprFileName = config.get(PRP_UPLOAD_FPR).orElse(null);
		if ( connHelper.isConnectionAvailable() && StringUtils.isNotBlank(fprFileName) ) {
			checkArtifactStatus(uploadFPRAndWaitForProcessingToComplete(fprFileName));
		}
	}
	
	@Override
	public void stop() {
		// Nothing to do
	}
	
	private String uploadFPRAndWaitForProcessingToComplete(String fprFileName) {
		int timeout = config.getInt(PRP_SSC_MAX_PROCESSING_TIMEOUT).orElse(DEFAULT_PROCESSING_TIMEOUT);
		File file = new File(fprFileName);
		if ( file.exists() && file.isFile() && file.canRead() ) {
			LOG.info("Uploading FPR file "+file.getAbsolutePath());
			return connHelper.getConnection().api(SSCArtifactAPI.class).uploadArtifactAndWaitProcessingCompletion(connHelper.getApplicationVersionId(), file, timeout);
		} else {
			throw new IllegalArgumentException("FPR file doesn't exist or is not readable: "+fprFileName);
		}
	}

	private void checkArtifactStatus(String artifactId) {
		Set<String> failOnArtifactStates = new HashSet<>(Arrays.asList(config.getStringArray(PRP_SSC_FAIL_ON_ARTIFACT_STATES)));
		if ( !failOnArtifactStates.isEmpty() && artifactId!=null ) {
			JSONMap artifact = connHelper.getConnection().api(SSCArtifactAPI.class).getArtifactById(artifactId, false);
			String status = artifact.get("status", String.class);
			if ( failOnArtifactStates.contains(status) ) {
				throw new IllegalStateException("Failing on SSC artifact status "+status);
			}
		}
	}
	
	public static final void addPropertyDefinitions(List<PropertyDefinition> propertyDefinitions) {
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
				.multiValues(true)
				.build());
		propertyDefinitions.add(PropertyDefinition.builder(PRP_UPLOAD_FPR)
				.name("FPR file to upload to SSC (optional)")
				.description("FPR file to upload to SSC")
				.type(PropertyType.STRING)
				.onlyOnQualifiers(Qualifiers.PROJECT)
				.build());
	}
}
