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
package com.fortify.integration.sonarqube.common.source.ssc;

import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.integration.sonarqube.common.IFortifyConnectionHelper;

/**
 * This interface provides access to the SSC URL (including credentials), the
 * corresponding {@link SSCAuthenticatingRestConnection} instance, and the
 * SSC application version id. It also provides a utility method for checking 
 * whether SSC connection and application version id are available.
 * 
 * @author Ruud Senden
 *
 */
public interface IFortifySSCConnectionHelper extends IFortifyConnectionHelper<SSCAuthenticatingRestConnection> {
	/**
	 * @return SSC filter set GUID, or null if not available/configured
	 */
	public String getFilterSetGuid();

	/**
	 * @return SSC application version id, or null if not available/configured
	 */
	public String getApplicationVersionId();

	/**
	 * @return SSC URL (including credentials), or null if not available/configured
	 */
	public String getSSCUrl();

}