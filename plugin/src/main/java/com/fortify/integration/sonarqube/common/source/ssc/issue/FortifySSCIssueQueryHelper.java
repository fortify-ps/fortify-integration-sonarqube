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
package com.fortify.integration.sonarqube.common.source.ssc.issue;

import com.fortify.client.ssc.api.SSCIssueAPI;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionIssuesQueryBuilder.QueryMode;
import com.fortify.integration.sonarqube.common.issue.AbstractFortifySourceSystemIssueQueryHelper;
import com.fortify.integration.sonarqube.common.source.ssc.IFortifySSCConnectionHelper;
import com.fortify.util.rest.query.IRestConnectionQuery;

public final class FortifySSCIssueQueryHelper extends AbstractFortifySourceSystemIssueQueryHelper<IFortifySSCConnectionHelper> {
	public static final String[] ISSUE_FIELD_NAMES = FortifySSCIssueFieldsRetriever.ISSUE_FIELD_NAMES;
	
	public FortifySSCIssueQueryHelper(IFortifySSCConnectionHelper connHelper) {
		super(connHelper);
	}

	@Override
	public final IRestConnectionQuery getAllIssuesQuery() {
		IFortifySSCConnectionHelper connHelper = getConnHelper();
		return connHelper.getConnection().api(SSCIssueAPI.class).queryIssues(connHelper.getApplicationVersionId())
			.paramFilterSet(connHelper.getFilterSetGuid())
			.paramFields(ISSUE_FIELD_NAMES)
			.paramShowHidden(false)
			.paramShowRemoved(false)
			.paramShowSuppressed(false)
			.paramQm(QueryMode.issues)
			.onDemandDetails()
			.build();
	}
}
