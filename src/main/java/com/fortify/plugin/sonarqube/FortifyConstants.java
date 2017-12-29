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
package com.fortify.plugin.sonarqube;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.sonar.api.batch.rule.Severity;

/**
 * This class defines various constants used throughout the plugin.
 * 
 * @author Ruud Senden
 *
 */
public final class FortifyConstants {
	private FortifyConstants() {}

	/** Formatter for the language-specific SonarQube Fortify rule key */
	private static final MessageFormat FMT_FTFY_RULE_KEY = new MessageFormat("fortify-{0}");
	/** Formatter for the language-specific SonarQube Fortify rule repository key */
	private static final MessageFormat FMT_FTFY_RULE_REPO_KEY = new MessageFormat("fortify-{0}");
	/** Map containing Fortify friority to SonarQube severity mapping */
	private static final Map<String,Severity> MAP_FRIORITY_TO_SEVERITY = getFriorityToSeverityMap();
	
	/** SonarQube key for the Fortify language, see {@link FortifyLanguage} */
	public static final String FTFY_LANGUAGE_KEY = "fortify";
	/** SonarQube key for the filter parameter for Fortify rules */
	public static final String RULE_PARAM_FILTER_KEY = "filter";
	
	/** SonarQube property holding the SSC URL */
	public static final String PRP_SSC_URL = "sonar.fortify.ssc.url";
	/** SonarQube property specifying whether issue data collection is enabled */
	public static final String PRP_ENABLE_ISSUES = "sonar.fortify.issues.enable";
	/** SonarQube property holding the filter set to use for issue collection */
	public static final String PRP_FILTER_SET = "sonar.fortify.ssc.filterset";
	/** SonarQube property holding file suffixes to be handled by {@link FortifyLanguage} */
	public static final String PRP_FILE_SUFFIXES = "sonar.fortify.filesuffixes";
	/** SonarQube property holding the SSC application version id or name */
	public static final String PRP_SSC_APP_VERSION = "sonar.fortify.ssc.appversion";
	/** SonarQube property holding the maximum number of seconds to wait for SSC to process the latest uploaded results */
	public static final String PRP_SSC_MAX_PROCESSING_TIMEOUT = "sonar.fortify.ssc.processing.timeout";
	/** SonarQube property specifying whether an exception should be thrown on the specified artifact states */
	public static final String PRP_SSC_FAIL_ON_ARTIFACT_STATES = "sonar.fortify.ssc.failOnArtifactStates";
	/** SonarQube property holding the FPR file name to upload */
	public static final String PRP_UPLOAD_FPR = "sonar.fortify.ssc.uploadFPR";
	
	
	/**
	 * Get the SonarQube key for the generic Fortify rule for the given language
	 * @param language
	 * @return
	 */
	public static final String FTFY_RULE_KEY(String language) {
		return FMT_FTFY_RULE_KEY.format(new String[]{language});
	}
	
	/**
	 * Get the SonarQube key for the Fortify rule repository for the given language
	 * @param language
	 * @return
	 */
	public static final String FTFY_RULE_REPO_KEY(String language) {
		return FMT_FTFY_RULE_REPO_KEY.format(new String[]{language});
	}
	
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
