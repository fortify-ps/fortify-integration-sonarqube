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
 * <p>This package contains common SonarQube plugin code that is shared between the
 * various version-specific plugin implementations. Any code in this package and
 * sub-packages should compile without any errors against all SonarQube API versions
 * that are supported by the plugin. Also, unless there is no work-around, all code
 * should compile against the latest supported SonarQube API version without any 
 * deprecation warnings. Compatibility with specific SonarQube API versions can be
 * easily verified using the various Maven profiles (see the root pom.xml file).</p>
 * 
 * <p>In general, this package provides the following functionality:</p>
 * <ul>
 *   <li>Helpers classes and common functionality like connection handling.</li>
 *   <li>Full implementations for SonarQube extensions that are shared as-is 
 *       between the various version-specific implementations; the SonarQube
 *       API's and annotations used by these extensions are fully supported
 *       and not deprecated for all supported SonarQube API versions.</p>
 *   <li>Partial implementations for SonarQube extensions for which a full
 *       implementation cannot be provided without depending on API's that
 *       have been deprecated in the latest SonarQube API version. The 
 *       version-specific packages extend these partial implementations by
 *       adding the version-specific SonarQube annotations.<br/><br/>
 *       Note that in most cases, we could have simply provided a single
 *       implementation used for all supported SonarQube API versions, using
 *       API's and annotations that are deprecated in the latest API version; 
 *       likely these deprecated API's are still supported by the current
 *       latest SonarQube version. However the use of deprecated annotations 
 *       and API's increases the likelihood that the plugin will no longer 
 *       function with future SonarQube versions, if support for these deprecated
 *       API's is dropped completely. As such, the plugin implementation for the 
 *       latest available SonarQube version (including any common code) should
 *       avoid the use of deprecated API's, to make the plugin as future-proof
 *       as possible.</li> 
 * </ul>
 * 
 * <p>In practice, most of the scanner/batch-side annotations and interfaces have 
 * been deprecated somewhere between SonarQube API versions 6.7 and 7.6. As such,
 * for all scanner/batch-side extensions, only a partial implementation is provided
 * in this common package.</p>
 * 
 * <p>In most cases, the version-specific extensions just needs to extend from the 
 * partial implementation class provided in the common package, and add the relevant 
 * version-specific annotations. For sensors, also some interfaces and bases classes
 * have been deprecated, so the version-specific implementations are further apart.</p>
 * 
 */

package com.fortify.integration.sonarqube.common;