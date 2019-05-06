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

/**
 * <p>This package contains SonarQube plugin code specific to SonarQube 7.6
 * and up. For earlier versions, the plugin code is provided
 * in the {@link com.fortify.integration.sonarqube.sq76.ssc} package.</p>
 * 
 * <p>Most of the classes provided in this package simply extend from a common 
 * implementation provided in the {@link com.fortify.integration.sonarqube.common.ssc} 
 * package, and add the SonarQube 7.6-specific {@link org.sonar.api.scanner.ScannerSide} 
 * annotation. The notable exception are sensor implementations, which require a 
 * more complete 7.6-specific implementations due to some major API changes between
 * SonarQube 6.7 and 7.6.</p>
 * 
 */

package com.fortify.integration.sonarqube.sq76;