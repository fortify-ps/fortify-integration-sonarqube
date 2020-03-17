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
package com.fortify.integration.sonarqube.sq67.source.ssc;

import org.sonar.api.Plugin.Context;
import org.springframework.stereotype.Component;

import com.fortify.integration.sonarqube.common.IFortifyExtensionProvider;
import com.fortify.integration.sonarqube.sq67.source.ssc.scanner.FortifySSCSQ67ConnectionPropertiesMetricsSensor;
import com.fortify.integration.sonarqube.sq67.source.ssc.scanner.FortifySSCSQ67IssueSensor;
import com.fortify.integration.sonarqube.sq67.source.ssc.scanner.FortifySSCSQ67ScannerSideConnectionHelper;
import com.fortify.integration.sonarqube.sq67.source.ssc.scanner.FortifySSCSQ67UploadFPRStartable;

@Component
public class FortifySSCSQ67ExtensionProvider implements IFortifyExtensionProvider {

	@Override
	public Class<?>[] getExtensions(Context context) {
		return new Class<?>[] {
			FortifySSCSQ67ScannerSideConnectionHelper.class,
			FortifySSCSQ67UploadFPRStartable.class,
			FortifySSCSQ67ConnectionPropertiesMetricsSensor.class,
			FortifySSCSQ67IssueSensor.class
		};
	}

}
