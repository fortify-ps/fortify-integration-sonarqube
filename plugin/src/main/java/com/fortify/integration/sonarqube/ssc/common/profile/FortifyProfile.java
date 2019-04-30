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
package com.fortify.integration.sonarqube.ssc.common.profile;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

import com.fortify.integration.sonarqube.ssc.common.FortifyConstants;
import com.fortify.integration.sonarqube.ssc.common.FortifySSCPlugin;
import com.fortify.integration.sonarqube.ssc.common.language.FortifyLanguage;
import com.fortify.integration.sonarqube.ssc.common.rule.FortifyRulesDefinition;

/**
 * <p>This class defines a default quality profile for the generic Fortify language 
 * (see {@link FortifyLanguage}) with all available rules activated by default.</p>
 * 
 * <p>This SonarQube extension is registered for all supported SonarQube 
 * versions by {@link FortifySSCPlugin}.</p>
 * 
 * @author Ruud Senden
 *
 */
public class FortifyProfile implements BuiltInQualityProfilesDefinition {
	private final FortifyRulesDefinition rulesDefinition;
	
	public FortifyProfile(FortifyRulesDefinition rulesDefinition) {
		this.rulesDefinition = rulesDefinition;
	}

	@Override
	public void define(Context context) {
		NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("Default", FortifyConstants.FTFY_LANGUAGE_KEY);
		for ( String ruleKey : rulesDefinition.getRuleKeys() ) {
			profile.activateRule("fortify", ruleKey);
		}
		profile.done();
	}

}
