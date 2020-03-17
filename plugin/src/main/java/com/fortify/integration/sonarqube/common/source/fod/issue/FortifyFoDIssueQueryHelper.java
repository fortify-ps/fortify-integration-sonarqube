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

import com.fortify.client.fod.api.FoDVulnerabilityAPI;
import com.fortify.integration.sonarqube.common.issue.AbstractFortifySourceSystemIssueQueryHelper;
import com.fortify.integration.sonarqube.common.source.fod.IFortifyFoDConnectionHelper;
import com.fortify.util.rest.query.IRestConnectionQuery;

public final class FortifyFoDIssueQueryHelper extends AbstractFortifySourceSystemIssueQueryHelper<IFortifyFoDConnectionHelper> {
	public FortifyFoDIssueQueryHelper(IFortifyFoDConnectionHelper connHelper) {
		super(connHelper);
	}

	@Override
	public final IRestConnectionQuery getAllIssuesQuery() {
		return getConnHelper().getConnection().api(FoDVulnerabilityAPI.class)
				.queryVulnerabilities(getConnHelper().getReleaseId())
				.paramFields(FortifyFoDIssueFieldsRetriever.ISSUE_FIELD_NAMES)
				.paramIncludeFixed(false)
				.paramIncludeSuppressed(false)
				.onDemandDetails(FortifyFoDIssueFieldsRetriever.ISSUE_FIELDS_ON_DEMAND.details.name())
				.onDemandRecommendations(FortifyFoDIssueFieldsRetriever.ISSUE_FIELDS_ON_DEMAND.recommendations.name())
				.build();
	}
}
