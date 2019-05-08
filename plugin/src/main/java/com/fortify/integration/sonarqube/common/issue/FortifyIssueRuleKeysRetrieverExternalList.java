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
package com.fortify.integration.sonarqube.common.issue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.integration.sonarqube.common.externalmetadata.ExternalCategory;
import com.fortify.integration.sonarqube.common.externalmetadata.ExternalList;
import com.fortify.integration.sonarqube.common.rule.FortifyRulesDefinition;
import com.fortify.util.rest.json.JSONMap;

public class FortifyIssueRuleKeysRetrieverExternalList implements IFortifyIssueRuleKeysRetriever {
	private static final Logger LOG = Loggers.get(FortifyIssueRuleKeysRetrieverExternalList.class);
	private final SensorContext context;
	private final ExternalList externalList;
	
	public FortifyIssueRuleKeysRetrieverExternalList(SensorContext context, ExternalList externalList) {
		this.context = context;
		this.externalList = externalList;
	}
	
	@Override
	public Collection<RuleKey> getRuleKeys(IFortifySourceSystemIssueFieldRetriever issueFieldRetriever, JSONMap issue) {
		String fortifyCategory = issueFieldRetriever.getCategory(issue);
		Collection<ExternalCategory> externalCategories = externalList.getExternalCategoriesForFortifyCategory(fortifyCategory);
		if ( externalCategories==null ) {
			LOG.debug("Fortify category "+fortifyCategory+" not mapped to any external categories");
			ActiveRule otherRule = context.activeRules().findByInternalKey(FortifyRulesDefinition.REPOSITORY_KEY, FortifyRulesDefinition.RULE_KEY_OTHER);
			return otherRule == null ? null : Arrays.asList(otherRule.ruleKey());
		} else {
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("Mapped fortify category "+fortifyCategory+" to external categories "+externalCategories);
			}
			Collection<RuleKey> result = new ArrayList<>();
			externalCategories.forEach(externalCategory-> {
					ActiveRule activeRule = context.activeRules().findByInternalKey(FortifyRulesDefinition.REPOSITORY_KEY, externalCategory.getName());
					if ( activeRule != null ) { result.add(activeRule.ruleKey()); }
				});
			return result;
		}
	}

}
