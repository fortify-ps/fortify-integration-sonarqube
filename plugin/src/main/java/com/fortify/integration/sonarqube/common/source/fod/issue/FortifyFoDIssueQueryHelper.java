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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.fortify.client.fod.api.FoDVulnerabilityAPI;
import com.fortify.client.fod.api.FoDVulnerabilityFiltersAPI;
import com.fortify.client.fod.api.query.builder.FoDReleaseVulnerabilitiesQueryBuilder;
import com.fortify.integration.sonarqube.common.externalmetadata.ExternalList;
import com.fortify.integration.sonarqube.common.issue.AbstractFortifySourceSystemIssueQueryHelper;
import com.fortify.integration.sonarqube.common.source.fod.IFortifyFoDConnectionHelper;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.query.IRestConnectionQuery;

public final class FortifyFoDIssueQueryHelper extends AbstractFortifySourceSystemIssueQueryHelper<IFortifyFoDConnectionHelper> {
	private static final String[] ISSUE_FIELD_NAMES = FortifyFoDIssueFieldsRetriever.ISSUE_FIELD_NAMES;
	private static final Map<String,String> EXTERNAL_LIST_NAME_TO_FIELD_MAP = getExternalListNameToFieldNameMap();
	
	public FortifyFoDIssueQueryHelper(IFortifyFoDConnectionHelper connHelper) {
		super(connHelper);
	}

	@Override
	public final boolean supportsExternalList(ExternalList externalList) {
		// FoD doesn't natively support external lists:
		// - We can map various external list names to various FoD issue fields, but not all external lists are supported by FoD
		// - Even for supported external lists, category names (field values) may be different than category names in externalmetadata.xml
		// For example, external list name 'OWASP Top 10 2017' could be mapped to FoD field owasp2017,
		// but FoD reports for example 'A2 - Broken Authentication' as the category, whereas the category name
		// in externalmetadata.xml is 'A2 Broken Authentication'. We could again map this with some fuzzy logic,
		// but it would be too hard to guarantee that this will work in all cases for all external lists.
		return false; 
	}

	@Override
	public final HashSet<String> getAvailableExternalCategories(ExternalList externalList) {
		// TODO throw UnsupportedOperationException
		String fieldName = getFieldName(externalList);
		JSONList vulnerabilityFilters = getConnHelper().getConnection().api(FoDVulnerabilityFiltersAPI.class)
				.queryVulnerabilityFilters(getConnHelper().getReleaseId())
				.paramFieldName(fieldName)
				.build().getAll();
		return new HashSet<>(vulnerabilityFilters
				.mapValue("fieldName", fieldName, "fieldFilterValues", JSONList.class).getValues("value", String.class));
	}

	@Override
	public final IRestConnectionQuery getAllIssuesQuery() {
		return getIssuesBaseQuery().build();
	}

	@Override
	public final IRestConnectionQuery getExternalCategoryIssuesQuery(ExternalList externalList, String externalCategory) {
		// TODO throw UnsupportedOperationException
		return getIssuesBaseQuery().paramFilterAnd(getFieldName(externalList), externalCategory).build();
	}
	
	private final FoDReleaseVulnerabilitiesQueryBuilder getIssuesBaseQuery() {
		FoDReleaseVulnerabilitiesQueryBuilder result = getConnHelper().getConnection().api(FoDVulnerabilityAPI.class)
			.queryVulnerabilities(getConnHelper().getReleaseId())
			.paramFields(ISSUE_FIELD_NAMES)
			.paramIncludeFixed(false)
			.paramIncludeSuppressed(false);
		return result;
	}
	
	private final String getFieldName(ExternalList externalList) {
		// TODO Remove this method
		return EXTERNAL_LIST_NAME_TO_FIELD_MAP.get(externalList.getName());
	}
	
	private static final Map<String, String> getExternalListNameToFieldNameMap() {
		// TODO Remove this method
		Map<String, String> result = new HashMap<>();
		result.put("NIST SP 800-53 Rev.4", "");
        result.put("CWE", "");
        result.put("OWASP Top 10 2004", "owasp2004");
        result.put("OWASP Top 10 2007", "owasp2007");
        result.put("OWASP Top 10 2010", "owasp2010");
        result.put("OWASP Top 10 2013", "owasp2013");
        result.put("OWASP Top 10 2017", "owasp2017");
        result.put("OWASP Mobile 2014", "owasp2014MobileTop10");
        result.put("SANS Top 25 2009", "sans2009");
        result.put("FISMA", "fisma");
        //result.put("PCI 1.1", "");
        //result.put("PCI 1.2", "");
        result.put("PCI 2.0", "pci2");
        result.put("PCI 3.0", "pci3");
        result.put("PCI 3.1", "pci3_1");
        result.put("PCI 3.2", "pci3_2");
        //result.put("PCI 3.2.1", "");
        //result.put("STIG 3.1", "");
        //result.put("STIG 3.4", "");
        //result.put("STIG 3.5", "");
        //result.put("STIG 3.6", "");
       // result.put("STIG 3.7", "");
        result.put("STIG 3.9", "sti3_9");
        //result.put("STIG 3.10", "");
        result.put("STIG 4.1", "sti4_1");
        //result.put("STIG 4.2", "");
        result.put("STIG 4.3", "sti4_3");
        //result.put("STIG 4.4", "");
        //result.put("STIG 4.5", "");
        //result.put("STIG 4.6", "");
        //result.put("STIG 4.7", "");
        //result.put("STIG 4.8", "");
        //result.put("STIG 4.9", "");
        //result.put("DISA CCI 2", "");
        result.put("WASC 24 + 2", "wasc24_2");
        //result.put("WASC 2.00", "");
        //result.put("SANS Top 25 2010", "");
        //result.put("SANS Top 25 2011", "");
        //result.put("MISRA C 2012", "");
        //result.put("MISRA C++ 2008", "");
        //result.put("GDPR", "");
		return result;
	}

}
