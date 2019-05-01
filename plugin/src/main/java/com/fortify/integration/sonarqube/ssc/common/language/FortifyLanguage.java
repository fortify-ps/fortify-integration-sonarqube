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
package com.fortify.integration.sonarqube.ssc.common.language;

import java.util.List;

import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.AbstractLanguage;

import com.fortify.integration.sonarqube.ssc.common.FortifyConstants;
import com.fortify.integration.sonarqube.ssc.common.FortifySSCPlugin;

/**
 * <p>This class defines a generic SonarQube language against which
 * all Fortify vulnerabilities are being reported, independent of the 
 * actual source code language. In addition, this language implementation
 * allows for for configuring additional file extensions that should be
 * processed by SonarQube.</p>

 * <p>Depending on which language plugins have been installed in SonarQube,
 * SonarQube only processes files with the file suffixes supported by
 * these language plugins. The Fortify scan results may include file 
 * extensions that are not handled by any of the installed SonarQube
 * language plugins; this would mean that vulnerabilities in these files
 * cannot be included in the SonarQube results.</p>
 * 
 * <p>Adding these missing file suffixes to the 
 * {@link FortifyConstants#PRP_FILE_SUFFIXES} property allows for
 * including these file types in the SonarQube results, such that the
 * corresponding Fortify vulnerabilities can be reported in SonarQube.</p>
 * 
 * <p>This SonarQube extension is registered for all supported SonarQube 
 * versions by {@link FortifySSCPlugin}.</p>
 * 
 * @author Ruud Senden
 *
 */
public class FortifyLanguage extends AbstractLanguage {
	/** SonarQube property holding file suffixes to be handled by this language class */
	private static final String PRP_FILE_SUFFIXES = "sonar.fortify.filesuffixes";
	
	private final Configuration config;

	/**
	 * Constructor to set the language properties and inject
	 * the SonarQube {@link Configuration}.
	 * @param config
	 */
	public FortifyLanguage(Configuration config) {
		super(FortifyConstants.FTFY_LANGUAGE_KEY, "Fortify");
		this.config = config;
	}

	/**
	 * Return the file suffixes as defined in the 
	 * {@link FortifyConstants#PRP_FILE_SUFFIXES} property
	 */
	@Override
	public String[] getFileSuffixes() {
		return config.getStringArray(PRP_FILE_SUFFIXES);
	}
	
	public static final void addPropertyDefinitions(List<PropertyDefinition> propertyDefinitions) {
		propertyDefinitions.add(PropertyDefinition.builder(PRP_FILE_SUFFIXES)
				.name("Additional file suffixes")
				// TODO Med: Reformat this description for better formatting
				//           on the plugin configuration page
				.description("(Optional) Additional file types to be included in the SonarQube scan to allow vulnerabilities"
						+ " to be reported on those file types. Multiple file suffixes can be separated by a comma."
						// TODO Check whether the following warning is still applicable for current SQ versions
						+ " DO NOT define any file suffixes that overlap with an existing SonarQube "
						+ " language plugin, as this will result in SonarQube errors.")
				.multiValues(true)
				.build());
	}
}
