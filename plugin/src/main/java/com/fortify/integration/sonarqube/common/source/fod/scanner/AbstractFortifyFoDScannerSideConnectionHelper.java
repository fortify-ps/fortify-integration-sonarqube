/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
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
package com.fortify.integration.sonarqube.common.source.fod.scanner;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.PropertyType;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import com.fortify.client.fod.api.FoDReleaseAPI;
import com.fortify.client.fod.connection.FoDAuthenticatingRestConnection;
import com.fortify.integration.sonarqube.common.FortifyConstants;
import com.fortify.util.rest.json.JSONMap;

/**
 * <p>FoD connection helper for scanner-side to get FoD connection instance and
 * release id. This abstract class provides all relevant functionality, but 
 * concrete implementations must add the appropriate SonarQube extension point
 * annotations.</p>
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractFortifyFoDScannerSideConnectionHelper implements IFortifyFoDScannerSideConnectionHelper {
	/** SonarQube property holding the FoD URL */
	private static final String PRP_FOD_URL = "sonar.fortify.fod.url";
	/** SonarQube property holding the FoD Tenant */
	private static final String PRP_FOD_TENANT = "sonar.fortify.fod.tenant";
	/** SonarQube property holding the FoD User Name */
	private static final String PRP_FOD_USER = "sonar.fortify.fod.user";
	/** SonarQube property holding the FoD Password */
	private static final String PRP_FOD_PWD = "sonar.fortify.fod.password";
	/** SonarQube property holding the FoD release id or name */
	private static final String PRP_FOD_RELEASE = "sonar.fortify.fod.release";
	
	private final Configuration config;
	private FoDAuthenticatingRestConnection connection;
	private String releaseId;
	
	/**
	 * Constructor for injecting dependencies
	 * @param config
	 */
	public AbstractFortifyFoDScannerSideConnectionHelper(Configuration config) {
		this.config = config;
	}
	
	/**
	 * Get the {@link FoDAuthenticatingRestConnection} based on the URL returned by {@link #getFoDUrl()}.
	 * The connection instance is cached for this {@link AbstractFortifyFoDScannerSideConnectionHelper}
	 * instance. If the connection is not available, this method returns null.
	 */
	@Override
	public final synchronized FoDAuthenticatingRestConnection getConnection() {
		String fodUrl = getFoDUrl();
		if ( connection==null && StringUtils.isNotBlank(fodUrl) && StringUtils.isNotBlank(getFoDTenant()) && StringUtils.isNotBlank(getFoDUser()) && StringUtils.isNotBlank(getFoDPassword()) ) {
			connection = FoDAuthenticatingRestConnection.builder().baseUrl(fodUrl).tenant(getFoDTenant()).userName(getFoDUser()).password(getFoDPassword()).build();
		}
		return connection;
	}
	
	/**
	 * Get the release id for the configured release name or id.
	 * If the FoD connection is not available, or no application version name or id has 
	 * been configured, this method returns null. 
	 */
	@Override
	public final synchronized String getReleaseId() {
		String releaseNameOrId = getReleaseNameOrId();
		if ( releaseId==null && StringUtils.isNotBlank(releaseNameOrId) && getConnection()!=null ) {
			JSONMap release = getConnection().api(FoDReleaseAPI.class).queryReleases()
					.nameOrId(releaseNameOrId)
					.paramFields("releaseId")
					.build().getUnique();
			if ( release != null ) {
				releaseId = release.get("releaseId", String.class);
			} else {
				throw new IllegalArgumentException("FoD release "+releaseNameOrId+" not found");
			}
		}
		return releaseId;
	}
	
	/**
	 * This method indicates whether FoD connection and release id 
	 * are available. 
	 */
	@Override
	public final boolean isConnectionAvailable() {
		return getConnection()!=null && getReleaseId()!=null; 
	}
	
	/**
	 * Get the configured FoD URL
	 */
	@Override
	public String getFoDUrl() {
		return config.get(PRP_FOD_URL).orElse(null);
	}
	
	/**
	 * Get the configured FoD Tenant
	 */
	@Override
	public String getFoDTenant() {
		return config.get(PRP_FOD_TENANT).orElse(null);
	}
	
	/**
	 * Get the configured FoD User
	 */
	@Override
	public String getFoDUser() {
		return config.get(PRP_FOD_USER).orElse(null);
	}
	
	/**
	 * Get the configured FoD Password
	 */
	@Override
	public String getFoDPassword() {
		return config.get(PRP_FOD_PWD).orElse(null);
	}
	
	
	/**
	 * Get the configured application version name or id
	 */
	@Override
	public String getReleaseNameOrId() {
		return config.get(PRP_FOD_RELEASE).orElse(null);
	}
	
	/**
	 * Add configuration properties that allow for specifying the SSC URL 
	 * (including credentials) and application version name or id.
	 * 
	 * @param propertyDefinitions
	 */
	public static final void addPropertyDefinitions(List<PropertyDefinition> propertyDefinitions) {
		propertyDefinitions.add(PropertyDefinition.builder(PRP_FOD_URL)
				.name("FoD URL")
				.description("(Required) URL used to connect to FoD (https://region.fortify.com/)")
				.type(PropertyType.STRING)
				.category(FortifyConstants.PROPERTY_CATEGORY_FOD)
				.build());
		
		propertyDefinitions.add(PropertyDefinition.builder(PRP_FOD_TENANT)
				.name("FoD Tenant")
				.description("(Required) FoD Tentant")
				.type(PropertyType.STRING)
				.category(FortifyConstants.PROPERTY_CATEGORY_FOD)
				.build());
		
		propertyDefinitions.add(PropertyDefinition.builder(PRP_FOD_USER)
				.name("FoD User")
				.description("(Required) FoD User")
				.type(PropertyType.STRING)
				.category(FortifyConstants.PROPERTY_CATEGORY_FOD)
				.build());
		
		propertyDefinitions.add(PropertyDefinition.builder(PRP_FOD_PWD)
				.name("FoD Password")
				.description("(Required) FoD Password")
				.type(PropertyType.PASSWORD)
				.category(FortifyConstants.PROPERTY_CATEGORY_FOD)
				.build());
		
		propertyDefinitions.add(PropertyDefinition.builder(PRP_FOD_RELEASE)
				.name("FoD Release")
				.description("(Required) FoD Release Id or Name (application:release).")
				.type(PropertyType.STRING)
				.onlyOnQualifiers(Qualifiers.PROJECT)
				.category(FortifyConstants.PROPERTY_CATEGORY_FOD)
				.build());
	}
}
