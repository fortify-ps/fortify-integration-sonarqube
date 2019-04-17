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
package com.fortify.integration.sonarqube.ssc.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.PropertyType;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;

import com.fortify.integration.sonarqube.ssc.FortifyConstants;
import com.fortify.integration.sonarqube.ssc.rule.externalmetadata.ExternalCategory;
import com.fortify.integration.sonarqube.ssc.rule.externalmetadata.ExternalList;
import com.fortify.integration.sonarqube.ssc.rule.externalmetadata.FortifyExternalMetadata;

/**
 * <p>This {@link RulesDefinition} implementation will generate Fortify-related
 * SonarQube rules.</p>
 */
public class FortifyRulesDefinition implements RulesDefinition {
	private static final String PRP_EXT_LIST_NAME = "fortify.externalListId";
	private static final FortifyExternalMetadata externalMetadata = FortifyExternalMetadata.parse(); 
	private final Configuration config;

	public FortifyRulesDefinition(Configuration config) {
		this.config = config;
	}
	
	private ExternalList getExternalList() {
		String externalListName = config.get(PRP_EXT_LIST_NAME).orElse(null);
		return externalMetadata==null || StringUtils.isBlank(externalListName) ? null : externalMetadata.getExternalListByName(externalListName);
	}
	
	@Override
	public void define(Context context) {
		NewRepository repo = context.createRepository("fortify", FortifyConstants.FTFY_LANGUAGE_KEY);
		repo.setName("Fortify");
		ExternalList externalList = getExternalList();
		if ( externalList != null ) {
			for ( ExternalCategory category : externalList.getExternalCategories().values() ) {
				repo.createRule(category.getId())
					.setName(category.getName())
					.setHtmlDescription(category.getDescription())
					.setType(RuleType.VULNERABILITY)
					.setTags("fortify")
					.setActivatedByDefault(true);
			}
		}
		repo.createRule("fortify.other")
			.setName("Other")
			.setHtmlDescription("This SonarQube rule is used for any vulnerabilities that are not mapped to any external category.")
			.setType(RuleType.VULNERABILITY)
			.setTags("fortify")
			.setActivatedByDefault(true);
		repo.done();
	}
	
	public Collection<String> getRuleKeys() {
		List<String> result = new ArrayList<>();
		ExternalList externalList = getExternalList();
		if ( externalList != null ) {
			externalList.getExternalCategories().values().forEach(c -> result.add(c.getId()));
		}
		result.add("fortify.other");
		return result;
	}
	
	public static final void addPropertyDefinitions(List<PropertyDefinition> propertyDefinitions) {
		propertyDefinitions.add(PropertyDefinition.builder(PRP_EXT_LIST_NAME)
				.name("Rules")
				.description("SonarQube rules definition")
				.type(PropertyType.SINGLE_SELECT_LIST)
				.options(new ArrayList<>(externalMetadata.getExternalListNames()))
				.build());
	}
}
