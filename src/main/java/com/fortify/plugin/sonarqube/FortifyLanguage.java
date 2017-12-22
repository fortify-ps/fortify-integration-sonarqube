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
package com.fortify.plugin.sonarqube;

import org.sonar.api.config.Settings;
import org.sonar.api.resources.AbstractLanguage;

/**
 * <p>This class defines a generic SonarQube language for all file suffixes
 * listed in the {@link FortifyConstants#PRP_FILE_SUFFIXES} property.</p>

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
 * @author Ruud Senden
 *
 */
public class FortifyLanguage extends AbstractLanguage {
	private final Settings settings;

	/**
	 * Constructor to set the language properties and inject
	 * the SonarQube {@link Settings}.
	 * @param settings
	 */
	public FortifyLanguage(Settings settings) {
		super(FortifyConstants.FTFY_LANGUAGE_KEY, "Other");
		this.settings = settings;
	}

	/**
	 * Return the file suffixes as defined in the 
	 * {@link FortifyConstants#PRP_FILE_SUFFIXES} property
	 */
	@Override
	public String[] getFileSuffixes() {
		return settings.getStringArray(FortifyConstants.PRP_FILE_SUFFIXES);
	}

}
