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
package com.fortify.plugin.sonarqube.rule;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Languages;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition;

import com.fortify.plugin.sonarqube.FortifyConstants;

/**
 * <p>This {@link RulesDefinition} implementation will generate Fortify-related
 * SonarQube rule repositories and rules.</p>
 * 
 * <p>SonarQube rules are defined at plug-in load time. Since Fortify 
 * products currently do not support retrieval of the full list of
 * available Fortify vulnerability categories (for both standard
 * and custom rules), this class simply defines a single Fortify
 * rule for every supported language. The actual vulnerability category 
 * will then be added to the description for each individual Fortify 
 * vulnerability.</p>
 */
public class FortifyRulesDefinition implements RulesDefinition {
	private final Settings settings;
	private final Languages languages;

	public FortifyRulesDefinition(Settings settings, Languages languages) {
		this.languages = languages;
		this.settings = settings;
	}
	
	@Override
	public void define(Context context) {
		for ( Language language : languages.all() ) {
			String languageKey = language.getKey();
			NewRepository repo = context.createRepository(FortifyConstants.FTFY_RULE_REPO_KEY(languageKey), languageKey);
			repo.setName("Fortify ("+language.getName()+")");
			NewRule rule = repo.createRule(FortifyConstants.FTFY_RULE_KEY(languageKey))
				.setName("Fortify ("+language.getName()+")")
				.setType(RuleType.VULNERABILITY)
				.setHtmlDescription(getClass().getClassLoader().getResource("FortifyRuleDescription.html"))
				.setTags("fortify");
			String sscUrl = settings.getString(FortifyConstants.PRP_SSC_URL);
			sscUrl = StringUtils.isNotBlank(sscUrl) ? sscUrl : "http://<ssc host>:<port>/ssc";
			rule.createParam(FortifyConstants.RULE_PARAM_FILTER_KEY)
				.setType(RuleParamType.TEXT)
				.setName("Filter")
				.setDescription("Report only Fortify vulnerabilities for which the given filter expression"
						+ " evaluates to true. Filter expression is a Spring Expression Language"
						+ " expression; see"
						+ " http://docs.spring.io/spring/docs/current/spring-framework-reference/html/expressions.html"
						+ " or"
						+ " http://www.baeldung.com/spring-expression-language"
						+ " for more details. For a list of fields that can be filtered on, please"
						+ " see the sample output at"
						+ " "+sscUrl+"/html/docs/api-reference/index.jsp#!/project-version-issue/getResourceCollectionUsingGET_50."
						+ " Note that this filter is only used for individual issues shown in SonarQube,"
						+ " not for issue count metrics (used for Quality Gates and on the Fortify dashboard)."
						+ " To avoid inconsistencies, you should usually let SSC handle the filtering based"
						+ " on SSC issue templates.");
			repo.done();
		}
	}
}
