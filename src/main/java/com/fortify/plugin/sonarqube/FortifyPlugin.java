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
package com.fortify.plugin.sonarqube;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import com.fortify.plugin.sonarqube.batch.FortifyAppVersionMetricsAndSensor;
import com.fortify.plugin.sonarqube.batch.FortifyArtifactMetricsAndSensor;
import com.fortify.plugin.sonarqube.batch.FortifyIssueMetricsAndSensor;
import com.fortify.plugin.sonarqube.batch.FortifySnapshotMetricsAndSensor;
import com.fortify.plugin.sonarqube.rule.FortifyRulesDefinition;
import com.fortify.plugin.sonarqube.ui.FortifyWidget;

/**
 * This main plugin class adds all relevant SonarQube extension points
 * that make up this Fortify integration.
 */
public class FortifyPlugin implements Plugin {
	
	@Override
	public void define(Context context) {
		// Add the plugin properties that can be configured for this plugin
		context.addExtensions(getProperties());
		context.addExtensions(
				// Extension point to handle SSC connection retrieval for extension points
				FortifySSCConnectionFactory.class,
				
				// Extension point for the Fortify Language
				FortifyLanguage.class,
				
				// Extension point for the Fortify Quality Profile
				FortifyProfile.class,
				
				// Extension point defining the SonarQube rules
				FortifyRulesDefinition.class,
				
				// Extension point for generating SonarQube violations based on Fortify issues
				FortifyIssueMetricsAndSensor.class,
				
				FortifyAppVersionMetricsAndSensor.class,
				FortifyArtifactMetricsAndSensor.class,
				FortifySnapshotMetricsAndSensor.class,
				
				// Extension point defining custom widget
				FortifyWidget.class
		);
	}
	
	/**
	 * Generate the configuration properties for the plug-in.
	 * @return
	 */
	private List<PropertyDefinition> getProperties() {
		List<PropertyDefinition> result = new ArrayList<PropertyDefinition>();
		result.add(PropertyDefinition.builder(FortifyConstants.PRP_SSC_URL)
				.name("SSC URL")
				.description("URL used to connect to SSC (http[s]://<user>:<password>@<host>[:port]/ssc or http[s]://authToken:token@<host>[:port]/ssc)")
				.type(PropertyType.PASSWORD)
				.build());
		result.add(PropertyDefinition.builder(FortifyConstants.PRP_ENABLE_ISSUES)
				.name("Enable issues collection")
				.description("Enable collecting Fortify issues")
				.type(PropertyType.BOOLEAN)
				.defaultValue("true")
				.build());
		result.add(PropertyDefinition.builder(FortifyConstants.PRP_SSC_MAX_PROCESSING_TIMEOUT)
				.name("Maximum processing time-out (seconds)")
				.description("Maximum amount of time SonarQube will wait for SSC to finish processing scan results")
				.type(PropertyType.INTEGER)
				.defaultValue("120")
				.build());
		result.add(PropertyDefinition.builder(FortifyConstants.PRP_SSC_FAIL_ON_ARTIFACT_STATES)
				.name("Fail scan on artifact states")
				.description("Fail the SonarQube scan if the SSC artifact state matches one of these comma-separated values."+
						" Valid states are PROCESS_COMPLETE, REQUIRE_AUTH, ERROR_PROCESSING")
				.type(PropertyType.STRING)
				.defaultValue("")
				.build());
		result.add(PropertyDefinition.builder(FortifyConstants.PRP_FILE_SUFFIXES)
				.name("Additional file suffixes")
				// TODO Med: Reformat this description for better formatting
				//           on the plugin configuration page
				.description("Additional file types to be included in the SonarQube scan to allow vulnerabilities"
						+ " to be reported on those file types. Multiple file suffixes can be separated by a comma."
						+ " DO NOT define any file suffixes that overlap with an existing SonarQube "
						+ " language plugin, as this will result in SonarQube errors.")
				.build());
		// TODO Can we dynamically get the list of projects/versions from SSC?
		result.add(PropertyDefinition.builder(FortifyConstants.PRP_SSC_APP_VERSION)
				.name("SSC Application Version")
				.description("SSC Application Version Id or Name (<application>:<version>).")
				.type(PropertyType.STRING)
				.onlyOnQualifiers(Qualifiers.PROJECT)
				.build());
		result.add(PropertyDefinition.builder(FortifyConstants.PRP_UPLOAD_FPR)
				.name("FPR file to upload to SSC (optional)")
				.description("FPR file to upload to SSC")
				.type(PropertyType.STRING)
				.onlyOnQualifiers(Qualifiers.PROJECT)
				.build());
		result.add(PropertyDefinition.builder(FortifyConstants.PRP_FILTER_SET)
				.name("Filter set id")
				.description("Filter set id used to retrieve issue data from SSC (optional)")
				.type(PropertyType.STRING)
				.onQualifiers(Qualifiers.PROJECT)
				.build());
		return result;
	}
}
