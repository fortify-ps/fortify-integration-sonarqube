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
package com.fortify.integration.sonarqube.common.ssc;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fortify.integration.sonarqube.common.IMetricsExpressionField;
import com.fortify.integration.sonarqube.common.MetricsExpressionFieldsHTMLDescriptionHelper;

public enum SSCMetricsExpressionField implements IMetricsExpressionField {
	filterSets("All fields returned by the /api/v1/projectVersions/${id}/filterSets endpoint"),
	performanceIndicatorHistories("All fields returned by the /api/v1/projectVersions/${id}/performanceIndicatorHistories endpoint"),
	variableHistories("All fields returned by the /api/v1/projectVersions/${id}/variableHistories endpoint"),
	var("Variable value by name, i.e. var['variableName']"),
	pi("Performance indicator value by name, i.e. pi['performanceIndicatorName']"),
	scaArtifact("For the most recent artifact that has not yet been processed, or most recent SCA artifact, all fields returned by the /api/v1/projectVersions/${id}/artifacts endpoint");
	
	
	private final String description;
	
	SSCMetricsExpressionField(String description) {
		this.description = description;
	}
	
	public String description() {
		return description;
	}
	
	public static final String getMetricsExpressionFieldsHTMLDescription() {
		final Map<String, String> examples = new LinkedHashMap<>();
		examples.put("name", "Application version name");
		examples.put("project.name", "Application name");
		examples.put("deepLink", "Deep link to application version");
		examples.put("pi['Fortify Security Rating']", "Performance Indicator 'Fortify Security Rating' value");
		examples.put("var['CFPO']", "Variable 'CFPO' value");
		examples.put("var['CFPO']+var['HFPO']", "Sum of variable values 'CFPO' and 'HFPO'");
		return MetricsExpressionFieldsHTMLDescriptionHelper
				.getMetricsExpressionFieldsHTMLDescription("/api/v1/projectVersions", SSCMetricsExpressionField.values(), examples);
	}
}