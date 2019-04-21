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

import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.ScannerSide;

import com.fortify.client.ssc.api.SSCApplicationVersionAPI;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.integration.sonarqube.ssc.FortifySSCConnectionProperties;
import com.fortify.util.rest.json.JSONMap;

/**
 * SSC connection helper for scanner-side to get SSC connection instance and
 * application version id. 
 * 
 * @author Ruud Senden
 *
 */
@ScannerSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class FortifySSCScannerSideConnectionHelper {
	private final FortifySSCConnectionProperties connectionProperties;
	private SSCAuthenticatingRestConnection connection;
	private String applicationVersionId;
	
	/**
	 * Constructor that initializes the connection instance
	 * @param config
	 */
	public FortifySSCScannerSideConnectionHelper(FortifySSCConnectionProperties connectionProperties) {
		this.connectionProperties = connectionProperties;
	}
	
	public final synchronized SSCAuthenticatingRestConnection getConnection() {
		String sscUrl = getSSCUrl();
		if ( connection==null && StringUtils.isNotBlank(sscUrl) ) {
			connection = SSCAuthenticatingRestConnection.builder().baseUrl(sscUrl).build();
		}
		return connection;
	}
	
	public final synchronized String getApplicationVersionId() {
		String applicationVersionNameOrId = getApplicationVersionNameOrId();
		if ( applicationVersionId==null && StringUtils.isNotBlank(applicationVersionNameOrId) && getConnection()!=null ) {
			JSONMap applicationVersion = getConnection().api(SSCApplicationVersionAPI.class).queryApplicationVersions()
					.nameOrId(applicationVersionNameOrId)
					.paramFields("id")
					.build().getUnique();
			if ( applicationVersion != null ) {
				applicationVersionId = applicationVersion.get("id", String.class);
			} else {
				throw new IllegalArgumentException("SSC application version "+applicationVersionNameOrId+" not found");
			}
		}
		return applicationVersionId;
	}
	
	/**
	 * @return true if the SSC connection is available, false otherwise
	 */
	public final boolean isConnectionAvailable() {
		return getConnection()!=null && getApplicationVersionId()!=null; 
	}
	
	public String getSSCUrl() {
		return connectionProperties.getSSCUrl();
	}

	public String getApplicationVersionNameOrId() {
		return connectionProperties.getApplicationVersionNameOrId();
	}
}
