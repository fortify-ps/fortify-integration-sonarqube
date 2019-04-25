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
package com.fortify.integration.sonarqube.ssc.common.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;

import com.fortify.integration.sonarqube.ssc.common.FortifyConstants;
import com.fortify.integration.sonarqube.ssc.config.RulesConfig;
import com.fortify.integration.sonarqube.ssc.externalmetadata.ExternalCategory;
import com.fortify.integration.sonarqube.ssc.externalmetadata.ExternalList;
import com.fortify.integration.sonarqube.ssc.externalmetadata.FortifyExternalMetadata;

/**
 * <p>This {@link RulesDefinition} implementation will generate Fortify-related
 * SonarQube rules.</p>
 */
public class FortifyRulesDefinition implements RulesDefinition {
	public static final String REPOSITORY_KEY = "fortify";
	public static final String RULE_KEY_OTHER = "fortify.other";
	private static final FortifyExternalMetadata externalMetadata = FortifyExternalMetadata.parse(); 
	private static final ExternalList externalList = _getExternalList();
	
	private static ExternalList _getExternalList() {
		String rulesSourceName = RulesConfig.load().getRulesSourceName();
		return externalMetadata==null || StringUtils.isBlank(rulesSourceName) || RulesConfig.SINGLE_RULE_SOURCE_NAME.equals(rulesSourceName) 
				? null : externalMetadata.getExternalListByName(rulesSourceName);
	}
	
	public static final String getExternalListId() {
		return externalList==null ? null : externalList.getId();
	}
	
	@Override
	public void define(Context context) {
		NewRepository repo = context.createRepository(REPOSITORY_KEY, FortifyConstants.FTFY_LANGUAGE_KEY);
		repo.setName("Fortify");
		if ( externalList != null ) {
			for ( ExternalCategory category : externalList.getExternalCategories().values() ) {
				repo.createRule(category.getId())
					.setInternalKey(category.getName())
					.setName(category.getName())
					.setHtmlDescription(category.getDescription())
					.setType(RuleType.VULNERABILITY)
					.setTags(REPOSITORY_KEY)
					.setActivatedByDefault(true);
			}
		}
		repo.createRule(RULE_KEY_OTHER)
			.setInternalKey(externalList==null?null:"[NONE]")
			.setName("Other")
			.setHtmlDescription("This SonarQube rule is used for any vulnerabilities that are not mapped to any external category.")
			.setType(RuleType.VULNERABILITY)
			.setTags(REPOSITORY_KEY)
			.setActivatedByDefault(true);
		repo.done();
	}
	
	public Collection<String> getRuleKeys() {
		List<String> result = new ArrayList<>();
		if ( externalList != null ) {
			externalList.getExternalCategories().values().forEach(c -> result.add(c.getId()));
		}
		result.add(RULE_KEY_OTHER);
		return result;
	}
}
