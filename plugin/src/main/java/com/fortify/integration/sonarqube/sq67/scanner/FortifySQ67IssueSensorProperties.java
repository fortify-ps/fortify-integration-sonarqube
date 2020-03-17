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
package com.fortify.integration.sonarqube.sq67.scanner;

import java.util.List;

import org.sonar.api.PropertyType;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;

import com.fortify.integration.sonarqube.common.FortifyConstants;

/**
 * This class provides configuration settings for, and access to, issue sensor properties.
 * This abstract class provides all relevant functionality, but concrete version-specific 
 * implementations must add the appropriate SonarQube extension point annotations. Where 
 * necessary, such concrete implementations may add support for additional properties.
 * 
 * @author Ruud Senden
 *
 */
@ScannerSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public final class FortifySQ67IssueSensorProperties {
	private static final String PRP_ENABLE_ISSUES = "sonar.fortify.issues.enable";
	private static final String PRP_REPORT_ISSUES_ONCE = "sonar.fortify.issues.reportOnce";
	private final Configuration config;
	
	public FortifySQ67IssueSensorProperties(Configuration config) {
		this.config = config;
	}
	
	/**
	 * @return True if issue collection is enabled or not configured, false otherwise
	 */
	public final boolean isIssueCollectionEnabled() {
		return config.getBoolean(PRP_ENABLE_ISSUES).orElse(true);
	}
	
	public final boolean isReportIssuesOnce() {
		return config.getBoolean(PRP_REPORT_ISSUES_ONCE).orElse(true);
	}
	
	/**
	 * Add configuration properties that allow for specifying whether issue collection is enabled,
	 * and whether issues should be reported only once.
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
		propertyDefinitions.add(PropertyDefinition.builder(PRP_REPORT_ISSUES_ONCE)
				.name("Report issues only once")
				.description("(Optional) Fortify issues are mapped to source files based on relative paths. As such, a single"
						+ " Fortify issue could be incorrectly mapped to similarly named source files in different modules."
						+ " If this property is set to false (default), the Fortify issue will be reported on"
						+ " every matching source file. If this property is set to true, the Fortify issue will"
						+ " be reported at most once. In either case, the Fortify issue may be reported on"
						+ " incorrect file(s)")
				.type(PropertyType.BOOLEAN)
				.defaultValue("false")
				.category(FortifyConstants.PROPERTY_CATEGORY_GENERIC)
				.build());
		// TODO Add property to specify whether non-SCA results should be loaded into SonarQube
	}
}
