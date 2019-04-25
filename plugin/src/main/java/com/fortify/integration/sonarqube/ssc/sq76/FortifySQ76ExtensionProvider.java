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
package com.fortify.integration.sonarqube.ssc.sq76;

import org.sonar.api.Plugin.Context;

import com.fortify.integration.sonarqube.ssc.common.IFortifyExtensionProvider;
import com.fortify.integration.sonarqube.ssc.sq76.ce.FortifySQ76ComputeEngineSideConnectionHelper;
import com.fortify.integration.sonarqube.ssc.sq76.ce.FortifySQ76ConfigurableMeasureComputer;
import com.fortify.integration.sonarqube.ssc.sq76.scanner.FortifySQ76IssuesSensor;
import com.fortify.integration.sonarqube.ssc.sq76.scanner.FortifySQ76ScannerSideConnectionHelper;
import com.fortify.integration.sonarqube.ssc.sq76.scanner.FortifySQ76UploadFPRStartable;

public class FortifySQ76ExtensionProvider implements IFortifyExtensionProvider {
	private static Class<?>[] SQ76_EXTENSIONS = {
			// Scanner-side extensions for handling SSC connection
			FortifySQ76ConnectionProperties.class,
			FortifySQ76ScannerSideConnectionHelper.class,
			
			// ComputeEngine-side extensions for handling SSC connection,
			// including scanner-side sensor for passing connection properties
			// from scanner-side to compute engine side
			FortifySQ76ComputeEngineSideConnectionHelper.SensorImpl.class,
			FortifySQ76ComputeEngineSideConnectionHelper.MetricsImpl.class,
			FortifySQ76ComputeEngineSideConnectionHelper.class,
			
			// Scanner-side extensions
			FortifySQ76UploadFPRStartable.class,
			FortifySQ76IssuesSensor.class,
			
			// ComputeEngine-side extensions
			FortifySQ76ConfigurableMeasureComputer.MetricsImpl.class,
			FortifySQ76ConfigurableMeasureComputer.class
	};

	@Override
	public Class<?>[] getExtensions(Context context) {
		return SQ76_EXTENSIONS;
	}

}
