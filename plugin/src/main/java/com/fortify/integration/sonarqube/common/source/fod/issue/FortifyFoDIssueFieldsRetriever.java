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
package com.fortify.integration.sonarqube.common.source.fod.issue;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.fortify.integration.sonarqube.common.issue.AbstractFortifySourceSystemIssueFieldRetriever;
import com.fortify.util.rest.json.JSONMap;

public final class FortifyFoDIssueFieldsRetriever extends AbstractFortifySourceSystemIssueFieldRetriever {
	private static enum ISSUE_FIELDS {
		// TODO Can we make FoDReleaseVulnerabilitiesQueryBuilder automatically add releaseId (and id) to paramFields if on-demand properties depend on these fields?
		id, releaseId, deepLink, scantype, category, severityString, lineNumber, primaryLocationFull;
		
		public <T> T get(JSONMap issue, Class<T> returnType) {
			return issue.get(name(), returnType);
		}
		
		public String get(JSONMap issue) {
			return get(issue, String.class);
		}
	}
	
	public static enum ISSUE_FIELDS_ON_DEMAND {
		details, recommendations, details_explanation, recommendations_recommendations, recommendations_tips, recommendations_references;
		
		public <T> T get(JSONMap issue, Class<T> returnType) {
			return issue.getPath(name().replace('_', '.'), returnType);
		}
		
		public String get(JSONMap issue) {
			return get(issue, String.class);
		}
	}
	
	public static final String[] ISSUE_FIELD_NAMES = Arrays.stream(ISSUE_FIELDS.values()).map(Enum::name).toArray(String[]::new);
	
	
	@Override
	public final String getId(JSONMap issue) { return ISSUE_FIELDS.id.get(issue); }

	@Override
	public final String getFileName(JSONMap issue) { return ISSUE_FIELDS.primaryLocationFull.get(issue); }

	@Override
	public final String getFriority(JSONMap issue) { return ISSUE_FIELDS.severityString.get(issue);	}

	@Override
	public final Integer getLineNumber(JSONMap issue) { return ISSUE_FIELDS.lineNumber.get(issue, Integer.class); }

	@Override
	public final String getCategory(JSONMap issue) { return ISSUE_FIELDS.category.get(issue); }

	@Override
	public final String getDeepLink(JSONMap issue) { return ISSUE_FIELDS.deepLink.get(issue); }
	
	@Override
	public String getRuleDescription(JSONMap issue) {
		String explanation = ISSUE_FIELDS_ON_DEMAND.details_explanation.get(issue);
		String recommendations = ISSUE_FIELDS_ON_DEMAND.recommendations_recommendations.get(issue);
		String tips = ISSUE_FIELDS_ON_DEMAND.recommendations_tips.get(issue);
		String references = ISSUE_FIELDS_ON_DEMAND.recommendations_references.get(issue);
		
		return "<b>Note that everything marked as 'As an example' may show an example that refers to an arbitrary Fortify issue that was loaded into SonarQube.</b>"
			+ getRuleDescriptionElt("Explanation", explanation) // TODO Can we get an issue-agnostic explanation?
			+ getRuleDescriptionElt("Recommendations", recommendations)
			+ getRuleDescriptionElt("Tips", tips)
			+ getRuleDescriptionElt("References", references);
	}
	
	private String getRuleDescriptionElt(String header, String value) {
		if ( StringUtils.isBlank(value) ) {
			return "";
		} else {
			return "<br/><br/><b>"+header+"</b><br/>"
				+value
					.replace("<paragraph>", "<p>")
					.replace("</paragraph>", "</p>")
					.replace("In this case", "As an example");
		}
	}
}
