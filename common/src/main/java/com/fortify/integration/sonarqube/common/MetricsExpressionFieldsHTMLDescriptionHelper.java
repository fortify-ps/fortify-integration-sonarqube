/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
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

import java.util.Map;

public class MetricsExpressionFieldsHTMLDescriptionHelper {
	public static final String getDescriptionHeader() {
		return "<p>Expressions define how to calculate the metric values."
				+ " For general information about these expressions, see the"
				+ " Spring Expression Language (SpEL) reference at"
				+ " https://docs.spring.io/spring/docs/4.3.22.RELEASE/spring-framework-reference/html/expressions.html."
				+ "</p>";
	}
	
	public static final String getMetricsExpressionFieldValuesAsHTMLListEntries(IMetricsExpressionField[] values) {
		StringBuffer sb = new StringBuffer();
		for ( IMetricsExpressionField field : values ) {
			sb.append("<li>").append(field.name()).append(" - ").append(field.description()).append("</li>");
		}
		return sb.toString();
	}
	
	public static final String getMapAsHTMLListEntries(Map<String, String> map) {
		StringBuffer sb = new StringBuffer();
		for ( Map.Entry<String, String> entry : map.entrySet() ) {
			sb.append("<li>").append(entry.getKey()).append(" - ").append(entry.getValue()).append("</li>");
		}
		return sb.toString();
	}
	
	public static final String getMetricsExpressionFieldsHTMLDescription(String mainEndpoint, IMetricsExpressionField[] values, Map<String, String> examples) {
		StringBuffer sb = new StringBuffer("<html>");
		sb.append(getDescriptionHeader());
		sb.append("<p>The following fields can be used in these expressions:</p>"
				+ "<ul>"
				+ "<li>All fields returned by the "+mainEndpoint+" endpoint</li>");
		sb.append(getMetricsExpressionFieldValuesAsHTMLListEntries(values));
		sb.append("</ul>");
		if ( examples != null ) {
			sb.append("<p>Following are some example expressions: </p><ul>");
			sb.append(getMapAsHTMLListEntries(examples));
			sb.append("</ul>");
		}
		
		sb.append("</html>");
		return sb.toString();
	}
}
