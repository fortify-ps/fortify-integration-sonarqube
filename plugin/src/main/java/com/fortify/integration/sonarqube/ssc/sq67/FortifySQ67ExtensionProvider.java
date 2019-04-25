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
package com.fortify.integration.sonarqube.ssc.sq67;

import org.sonar.api.Plugin.Context;

import com.fortify.integration.sonarqube.ssc.common.IFortifyExtensionProvider;
import com.fortify.integration.sonarqube.ssc.sq67.ce.FortifySQ67ComputeEngineSideConnectionHelper;
import com.fortify.integration.sonarqube.ssc.sq67.ce.FortifySQ67ConfigurableMeasureComputer;
import com.fortify.integration.sonarqube.ssc.sq67.scanner.FortifySQ67ScannerSideConnectionHelper;
import com.fortify.integration.sonarqube.ssc.sq67.scanner.FortifySQ67UploadFPRStartable;

/**
 * This main plugin class adds all relevant SonarQube extension points that make
 * up this Fortify integration.
 */
public class FortifySQ67ExtensionProvider implements IFortifyExtensionProvider {
	private static Class<?>[] SQ67_EXTENSIONS = {
			// Scanner-side extensions for handling SSC connection
			FortifySQ67ConnectionProperties.class,
			FortifySQ67ScannerSideConnectionHelper.class,
			
			// ComputeEngine-side extensions for handling SSC connection,
			// including scanner-side sensor for passing connection properties
			// from scanner-side to compute engine side
			FortifySQ67ComputeEngineSideConnectionHelper.SensorImpl.class,
			FortifySQ67ComputeEngineSideConnectionHelper.MetricsImpl.class,
			FortifySQ67ComputeEngineSideConnectionHelper.class,
			
			// Scanner-side extensions
			FortifySQ67UploadFPRStartable.class,
			// FortifySQ67IssuesSensor.class,
			
			// ComputeEngine-side extensions
			FortifySQ67ConfigurableMeasureComputer.MetricsImpl.class,
			FortifySQ67ConfigurableMeasureComputer.class
	};

	@Override
	public Class<?>[] getExtensions(Context context) {
		return SQ67_EXTENSIONS;
	}

}
