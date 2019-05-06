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
package com.fortify.integration.sonarqube.sq76;

import org.sonar.api.Plugin.Context;

import com.fortify.integration.sonarqube.common.FortifyPlugin;
import com.fortify.integration.sonarqube.common.IFortifyExtensionProvider;
import com.fortify.integration.sonarqube.sq76.ssc.FortifySSCSQ76ConnectionProperties;
import com.fortify.integration.sonarqube.sq76.ssc.ce.FortifySSCSQ76ComputeEngineSideConnectionHelper;
import com.fortify.integration.sonarqube.sq76.ssc.ce.FortifySSCSQ76ConfigurableMeasureComputer;
import com.fortify.integration.sonarqube.sq76.ssc.scanner.FortifySSCSQ76IssueSensor;
import com.fortify.integration.sonarqube.sq76.ssc.scanner.FortifySSCSQ76ScannerSideConnectionHelper;
import com.fortify.integration.sonarqube.sq76.ssc.scanner.FortifySSCSQ76UploadFPRStartable;

/**
 * This {@link IFortifyExtensionProvider} implementation provides the
 * 7.6-specific SonarQube extensions. This class, and the corresponding
 * extensions, are loaded by {@link FortifyPlugin}. 
 */
public class FortifySQ76ExtensionProvider implements IFortifyExtensionProvider {
	private static Class<?>[] SQ76_EXTENSIONS = {
			// Scanner-side extensions for handling SSC connection
			FortifySSCSQ76ConnectionProperties.class,
			FortifySSCSQ76ScannerSideConnectionHelper.class,
			
			// ComputeEngine-side extensions for handling SSC connection,
			// including scanner-side sensor for passing connection properties
			// from scanner-side to compute engine side
			FortifySSCSQ76ComputeEngineSideConnectionHelper.SensorImpl.class,
			FortifySSCSQ76ComputeEngineSideConnectionHelper.MetricsImpl.class,
			FortifySSCSQ76ComputeEngineSideConnectionHelper.class,
			
			// Scanner-side extensions
			FortifySSCSQ76UploadFPRStartable.class,
			FortifySSCSQ76IssueSensor.SensorProperties.class,
			FortifySSCSQ76IssueSensor.class,
			
			// ComputeEngine-side extensions
			FortifySSCSQ76ConfigurableMeasureComputer.MetricsImpl.class,
			FortifySSCSQ76ConfigurableMeasureComputer.class
	};

	@Override
	public Class<?>[] getExtensions(Context context) {
		return SQ76_EXTENSIONS;
	}

}
