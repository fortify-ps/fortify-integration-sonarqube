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
package com.fortify.integration.sonarqube.common.source.fod.issue;

import java.util.Arrays;

import com.fortify.integration.sonarqube.common.issue.IFortifySourceSystemIssueFieldRetriever;
import com.fortify.util.rest.json.JSONMap;

public final class FortifyFoDIssueFieldsRetriever implements IFortifySourceSystemIssueFieldRetriever {
	private static enum ISSUE_FIELDS {
		id, deepLink, scantype, category, severityString, lineNumber, primaryLocationFull;
		
		public <T> T get(JSONMap issue, Class<T> returnType) {
			return issue.get(name(), returnType);
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
	public final String getDeepLink(JSONMap issue) { return ISSUE_FIELDS.deepLink.get(issue);	}
}
