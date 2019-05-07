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
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.scanner.ScannerSide;

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
public final class FortifySQ76IssueSensorProperties {
	private static final String PRP_ENABLE_ISSUES = "sonar.fortify.issues.enable";
	
	/**
	 * @param context {@link SensorContext}
	 * @return True if issue collection is enabled or not configured, false otherwise
	 */
	public final boolean isIssueCollectionEnabled(SensorContext context) {
		return context.config().getBoolean(PRP_ENABLE_ISSUES).orElse(true);
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
				.build());
		// TODO Add property to specify whether non-SCA results should be loaded into SonarQube
	}
}
