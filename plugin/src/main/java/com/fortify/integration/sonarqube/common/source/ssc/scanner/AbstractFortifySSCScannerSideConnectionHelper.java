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
package com.fortify.integration.sonarqube.common.source.ssc.scanner;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.PropertyType;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import com.fortify.client.ssc.api.SSCApplicationVersionAPI;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.rest.json.JSONMap;

/**
 * <p>SSC connection helper for scanner-side to get SSC connection instance and
 * application version id. This abstract class provides all relevant functionality,
 * but concrete implementations must add the appropriate SonarQube extension point
 * annotations.</p>
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractFortifySSCScannerSideConnectionHelper implements IFortifySSCScannerSideConnectionHelper {
	/** SonarQube property holding the SSC URL */
	private static final String PRP_SSC_URL = "sonar.fortify.ssc.url";
	/** SonarQube property holding the SSC application version id or name */
	private static final String PRP_SSC_APP_VERSION = "sonar.fortify.ssc.appversion";
	
	private final Configuration config;
	private SSCAuthenticatingRestConnection connection;
	private String applicationVersionId;
	
	/**
	 * Constructor for injecting dependencies
	 * @param config
	 */
	public AbstractFortifySSCScannerSideConnectionHelper(Configuration config) {
		this.config = config;
	}
	
	/**
	 * Get the {@link SSCAuthenticatingRestConnection} based on the URL returned by {@link #getSSCUrl()}.
	 * The connection instance is cached for this {@link AbstractFortifySSCScannerSideConnectionHelper}
	 * instance. If the connection is not available, this method returns null.
	 */
	@Override
	public final synchronized SSCAuthenticatingRestConnection getConnection() {
		String sscUrl = getSSCUrl();
		if ( connection==null && StringUtils.isNotBlank(sscUrl) ) {
			connection = SSCAuthenticatingRestConnection.builder().baseUrl(sscUrl).build();
		}
		return connection;
	}
	
	/**
	 * Get the application version id for the configured application version name or id.
	 * If the SSC connection is not available, or no application version name or id has 
	 * been configured, this method returns null. 
	 */
	@Override
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
	 * This method indicates whether SSC connection and application version id 
	 * are available. 
	 */
	@Override
	public final boolean isConnectionAvailable() {
		return getConnection()!=null && getApplicationVersionId()!=null; 
	}
	
	/**
	 * Get the configured SSC URL
	 */
	@Override
	public String getSSCUrl() {
		return config.get(PRP_SSC_URL).orElse(null);
	}
	
	/**
	 * Get the configured application version name or id
	 */
	@Override
	public String getApplicationVersionNameOrId() {
		return config.get(PRP_SSC_APP_VERSION).orElse(null);
	}
	
	/**
	 * Add configuration properties that allow for specifying the SSC URL 
	 * (including credentials) and application version name or id.
	 * 
	 * @param propertyDefinitions
	 */
	public static final void addPropertyDefinitions(List<PropertyDefinition> propertyDefinitions) {
		propertyDefinitions.add(PropertyDefinition.builder(PRP_SSC_URL)
				.name("SSC URL")
				.description("(Required) URL used to connect to SSC (http[s]://user:password@host[:port]/ssc or http[s]://authToken:token@host[:port]/ssc)")
				.type(PropertyType.PASSWORD)
				.build());
		
		propertyDefinitions.add(PropertyDefinition.builder(PRP_SSC_APP_VERSION)
				.name("SSC Application Version")
				.description("(Required) SSC Application Version Id or Name (application:version).")
				.type(PropertyType.STRING)
				.onlyOnQualifiers(Qualifiers.PROJECT)
				.build());
	}
}
