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
package com.fortify.integration.sonarqube.sq76.scanner;

import java.util.List;

import org.sonar.api.PropertyType;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.scanner.ScannerSide;

import com.fortify.integration.sonarqube.common.FortifyConstants;

/**
 * This class provides configuration settings for, and access to, issue sensor properties.
 * 
 * @author Ruud Senden
 *
 */
@ScannerSide
public final class FortifySQ76IssueSensorProperties {
	private static final String PRP_ENABLE_ISSUES = "sonar.fortify.issues.enable";
	private static final String PRP_USE_AD_HOC_RULES = "sonar.fortify.issues.useAdHocRules";
	private static final String PRP_USE_STANDARD_RULES = "sonar.fortify.issues.useStandardRules";
	private static final String PRP_DEBUG_RULES_HTML_OUTPUT_FILE = "sonar.fortify.debug.rulesHtmlOutputFile";
	private final Configuration config;
	
	public FortifySQ76IssueSensorProperties(Configuration config) {
		this.config = config;
	}
	
	/**
	 * @return True if issue collection is enabled or not configured, false otherwise
	 */
	public final boolean isIssueCollectionEnabled() {
		return config.getBoolean(PRP_ENABLE_ISSUES).orElse(true);
	}
	
	/**
	 * TODO Decide whether this or standard rules should be enabled by default
	 * @return True if 'use ad hoc rules' is enabled, false if not configured or disabled
	 */
	public final boolean useAdHocRules() {
		return config.getBoolean(PRP_USE_AD_HOC_RULES).orElse(false);
	}
	
	/**
	 * TODO Decide whether this or ad hoc rules should be enabled by default
	 * @return True if 'use standard rules' is enabled or not configured, false otherwise
	 */
	public final boolean useStandardRules() {
		return config.getBoolean(PRP_USE_STANDARD_RULES).orElse(true);
	}
	
	/**
	 * @return The rules HTML File to generate for debugging, or null if not defined
	 */
	public final String getDebugRulesHtmlOutputFile() {
		return config.get(PRP_DEBUG_RULES_HTML_OUTPUT_FILE).orElse(null);
	}
	
	/**
	 * Add configuration property that allows for specifying whether issue collection is enabled.
	 * 
	 * @param propertyDefinitions
	 */
	public static void addPropertyDefinitions(List<PropertyDefinition> propertyDefinitions) {
		propertyDefinitions.add(PropertyDefinition.builder(PRP_ENABLE_ISSUES)
				.name("Enable issues collection")
				.description("(Optional) Enable collecting Fortify issues")
				.type(PropertyType.BOOLEAN)
				.defaultValue("true")
				.category(FortifyConstants.PROPERTY_CATEGORY_GENERIC)
				.build());
		propertyDefinitions.add(PropertyDefinition.builder(PRP_USE_AD_HOC_RULES)
				.name("Report issues based on ad-hoc rules")
				.description("(Optional) If enabled, ad-hoc rules are generated for Fortify categories, "
						+ "and issues reported against these ad-hoc rules. "
						+ "Note that this setting is only applicable for issues reported at file level; "
						+ "issues reported at project level are always reported against standard rules. "
						+ "Note that enabling both ad-hoc and standard rules will result in issues "
						+ "being reported twice.")
				.type(PropertyType.BOOLEAN)
				.defaultValue("false")
				.category(FortifyConstants.PROPERTY_CATEGORY_GENERIC)
				.build());
		propertyDefinitions.add(PropertyDefinition.builder(PRP_USE_STANDARD_RULES)
				.name("Report issues based on standard rules")
				.description("(Optional) If enabled, issues are reported against standard SonarQube rules. "
						+ "Note that this setting is only applicable for issues reported at file level; "
						+ "issues reported at project level are always reported against standard rules."
						+ "Note that enabling both ad-hoc and standard rules will result in issues "
						+ "being reported twice.")
				.type(PropertyType.BOOLEAN)
				.defaultValue("true")
				.category(FortifyConstants.PROPERTY_CATEGORY_GENERIC)
				.build());
		propertyDefinitions.add(PropertyDefinition.builder(PRP_DEBUG_RULES_HTML_OUTPUT_FILE)
				.name("Rules HTML output file")
				.description("(Optional) Used for debugging only; generates an HTML file with the given name, containing all Fortify rule descriptions for processed issues")
				.type(PropertyType.STRING)
				.category(FortifyConstants.PROPERTY_CATEGORY_GENERIC)
				.build());
		// TODO Add property to specify whether non-SCA results should be loaded into SonarQube
	}
}
