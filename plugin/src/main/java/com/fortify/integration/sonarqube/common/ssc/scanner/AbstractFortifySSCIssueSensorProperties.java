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
package com.fortify.integration.sonarqube.common.ssc.scanner;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import com.fortify.client.ssc.api.SSCIssueTemplateAPI;
import com.fortify.integration.sonarqube.common.FortifyConstants;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;

/**
 * This class provides configuration settings for, and access to, issue sensor properties.
 * This abstract class provides all relevant functionality, but concrete version-specific 
 * implementations must add the appropriate SonarQube extension point annotations. Where 
 * necessary, such concrete implementations may add support for additional properties.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractFortifySSCIssueSensorProperties {
	private static final String PRP_ENABLE_ISSUES = "sonar.fortify.issues.enable";
	private static final String PRP_FILTER_SET = "sonar.fortify.ssc.filterset";
	
	private final IFortifySSCScannerSideConnectionHelper connHelper;
	
	/**
	 * Constructor for injecting configuration
	 * @param config
	 */
	public AbstractFortifySSCIssueSensorProperties(IFortifySSCScannerSideConnectionHelper connHelper) {
		this.connHelper = connHelper;
	}
	
	/**
	 * @param context {@link SensorContext}
	 * @return True if issue collection is enabled or not configured, false otherwise
	 */
	public final boolean isIssueCollectionEnabled(SensorContext context) {
		return context.config().getBoolean(PRP_ENABLE_ISSUES).orElse(true);
	}
	
	/**
	 * @param context {@link SensorContext}
	 * @return The configured filter set name or id, or null if not configured
	 */
	public final String getFilterSetNameOrId(SensorContext context) {
		return context.config().get(PRP_FILTER_SET).orElse(null);
	}
	
	/**
	 * Get the filter set specified through the {@link FortifyConstants#PRP_FILTER_SET} setting
	 * @param context sensor context
	 * @param conn SSC connection
	 * @return JSONObject representing the specified filter set, or the default filter set if not specified
	 * @throws IllegalArgumentException if specified filter set cannot be found
	 */
	public final String getFilterSetId(SensorContext context) {
		JSONMap filterSet = null;
		JSONList filterSets = connHelper.getConnection().api(SSCIssueTemplateAPI.class).queryApplicationVersionFilterSets(connHelper.getApplicationVersionId()).build().getAll();
		String filterSetGuidOrTitle = context.config().get(PRP_FILTER_SET).orElse(null);
		if ( StringUtils.isNotBlank(filterSetGuidOrTitle) ) {
			String matchExpr = MessageFormat.format("guid==''{0}'' || title==''{0}''", new Object[]{filterSetGuidOrTitle});
			filterSet = filterSets.find(matchExpr, true, JSONMap.class);
			if ( filterSet==null ) {
				throw new IllegalArgumentException("Unknown filter set "+filterSetGuidOrTitle);
			}
		}
		return (filterSet==null ? getSSCDefaultFilterSet(filterSets) : filterSet).get("guid", String.class);
	}
	
	/**
	 * Find the SSC default filterset in the given {@link JSONList} 
	 * @param filterSets
	 * @return
	 */
	private final JSONMap getSSCDefaultFilterSet(JSONList filterSets) {
		return filterSets.find("defaultFilterSet", true, JSONMap.class);
	}
	
	/**
	 * Add configuration properties that allow for specifying whether issue collection
	 * is enabled, and which filter set to use for issue collection.
	 * 
	 * @param propertyDefinitions
	 */
	public static void addPropertyDefinitions(List<PropertyDefinition> propertyDefinitions) {
		propertyDefinitions.add(PropertyDefinition.builder(PRP_ENABLE_ISSUES)
				.name("Enable issues collection")
				.description("(Optional) Enable collecting Fortify issues")
				.type(PropertyType.BOOLEAN)
				.defaultValue("true")
				.build());
		propertyDefinitions.add(PropertyDefinition.builder(PRP_FILTER_SET)
				.name("Filter set name/id")
				.description("(Optional) Filter set name or id used to retrieve issue data from SSC")
				.type(PropertyType.STRING)
				.onQualifiers(Qualifiers.PROJECT)
				.build());
		// TODO Add property to specify whether non-SCA results should be loaded into SonarQube
	}
}
