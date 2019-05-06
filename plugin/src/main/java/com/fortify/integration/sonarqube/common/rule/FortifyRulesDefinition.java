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
package com.fortify.integration.sonarqube.common.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;

import com.fortify.integration.sonarqube.common.FortifyConstants;
import com.fortify.integration.sonarqube.common.FortifyPlugin;
import com.fortify.integration.sonarqube.common.config.RulesConfig;
import com.fortify.integration.sonarqube.common.externalmetadata.ExternalCategory;
import com.fortify.integration.sonarqube.common.externalmetadata.ExternalList;
import com.fortify.integration.sonarqube.common.externalmetadata.FortifyExternalMetadata;
import com.fortify.integration.sonarqube.common.profile.FortifyProfile;

/**
 * <p>This {@link RulesDefinition} implementation will generate Fortify-related
 * SonarQube rules. Based on {@link RulesConfig} and {@link FortifyExternalMetadata},
 * this implementation will add a default 'Other' rule, and optionally rules
 * corresponding to external list categories for the configured external list
 * name.</p>
 * 
 * <p>This SonarQube extension is registered for all supported SonarQube 
 * versions by {@link FortifyPlugin}.</p>
 */
public class FortifyRulesDefinition implements RulesDefinition {
	public static final String REPOSITORY_KEY = "fortify";
	public static final String RULE_KEY_OTHER = "fortify.other";
	private static final FortifyExternalMetadata externalMetadata = FortifyExternalMetadata.parse(); 
	private static final ExternalList externalList = _getExternalList();
	
	/**
	 * If the configured rules source name equals {@link RulesConfig#SINGLE_RULE_SOURCE_NAME},
	 * this method returns null. Otherwise, it returns the {@link ExternalList} instance
	 * corresponding to the configured rules source name.
	 * @return
	 */
	private static ExternalList _getExternalList() {
		String rulesSourceName = RulesConfig.load().getRulesSourceName();
		return externalMetadata==null || StringUtils.isBlank(rulesSourceName) || RulesConfig.SINGLE_RULE_SOURCE_NAME.equals(rulesSourceName) 
				? null : externalMetadata.getExternalListByName(rulesSourceName);
	}
	
	/**
	 * @return The external list id if configured, otherwise null.
	 */
	public static final String getExternalListId() {
		return externalList==null ? null : externalList.getId();
	}
	
	/**
	 * @return The external list name if configured, otherwise null.
	 */
	public static final String getExternalListName() {
		return externalList==null ? null : externalList.getName();
	}
	
	/**
	 * This method defines a new 'Fortify' rules repository. If an external
	 * list has been configured, each external list category is added as a
	 * SonarQube rule to this repository. In addition, independent of whether
	 * an external list has been configured or not, a default 'Other' rule is
	 * added to the repository.
	 */
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
	
	/**
	 * Get the rule keys defined by this {@link RulesDefinition} implementation.
	 * This is used by {@link FortifyProfile} to create a 'Fortify' profile with
	 * all Fortify-related rules enabled by default.
	 * @return
	 */
	public Collection<String> getRuleKeys() {
		List<String> result = new ArrayList<>();
		if ( externalList != null ) {
			externalList.getExternalCategories().values().forEach(c -> result.add(c.getId()));
		}
		result.add(RULE_KEY_OTHER);
		return result;
	}
}
