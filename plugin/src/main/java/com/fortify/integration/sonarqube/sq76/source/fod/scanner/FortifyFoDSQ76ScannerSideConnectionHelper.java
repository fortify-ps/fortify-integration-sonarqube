/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
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
package com.fortify.integration.sonarqube.sq76.source.fod.scanner;

import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;

import com.fortify.integration.sonarqube.common.source.fod.scanner.AbstractFortifyFoDScannerSideConnectionHelper;

/**
 * This {@link AbstractFortifyFoDScannerSideConnectionHelper} implementation just adds the
 * 7.6-specific {@link ScannerSide} annotation.
 * 
 * @author Ruud Senden
 *
 */
@ScannerSide
public class FortifyFoDSQ76ScannerSideConnectionHelper extends AbstractFortifyFoDScannerSideConnectionHelper {
	public FortifyFoDSQ76ScannerSideConnectionHelper(Configuration config) {
		super(config);
	}
}
