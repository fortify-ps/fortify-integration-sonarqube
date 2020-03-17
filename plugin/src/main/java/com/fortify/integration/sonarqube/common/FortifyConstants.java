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
package com.fortify.integration.sonarqube.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.sonar.api.batch.rule.Severity;

import com.fortify.integration.sonarqube.common.language.FortifyLanguage;

/**
 * This class defines various constants used throughout the plugin.
 * 
 * @author Ruud Senden
 *
 */
public final class FortifyConstants {
	private FortifyConstants() {}
	
	/** SonarQube key for the Fortify language, see {@link FortifyLanguage} */
	public static final String FTFY_LANGUAGE_KEY = "fortify";

	/** Map containing Fortify friority to SonarQube severity mapping */
	private static final Map<String,Severity> MAP_FRIORITY_TO_SEVERITY = getFriorityToSeverityMap();

	public static final String PROPERTY_CATEGORY_GENERIC = "Fortify";
	public static final String PROPERTY_CATEGORY_FOD = "Fortify - FoD";
	public static final String PROPERTY_CATEGORY_SSC = "Fortify - SSC";
	
	/** 
	 * Get the SonarQube severity for the given Fortify friority
	 * @param friority
	 * @return
	 */
	public static final Severity FRIORITY_TO_SEVERITY(String friority) {
		return MAP_FRIORITY_TO_SEVERITY.getOrDefault(friority, Severity.INFO);
	}
	
	/**
	 * Generate the static map containing Fortify friority to SonarQube severity mapping
	 * @return
	 */
	private static final Map<String, Severity> getFriorityToSeverityMap() {
		// TODO Med: Review this mapping
		Map<String,Severity> result = new HashMap<String,Severity>();
		result.put("critical", Severity.BLOCKER);
		result.put("high", Severity.CRITICAL);
		result.put("medium", Severity.MAJOR);
		result.put("low", Severity.MINOR);
		return Collections.unmodifiableMap(result);
	}
}
