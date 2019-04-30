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
package com.fortify.integration.sonarqube.ssc.common;

import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;

/**
 * This interface provides access to the SSC URL (including credentials), the
 * corresponding {@link SSCAuthenticatingRestConnection} instance, and the
 * SSC application version id. It also provides a utility method for checking 
 * whether SSC connection and application version id are available.
 * 
 * @author Ruud Senden
 *
 */
public interface IFortifyConnectionHelper {

	/**
	 * @return {@link SSCAuthenticatingRestConnection} instance, or null if not available/configured
	 */
	public SSCAuthenticatingRestConnection getConnection();

	/**
	 * @return SSC application version id, or null if not available/configured
	 */
	public String getApplicationVersionId();

	/**
	 * @return true if the SSC connection and application version id is available, false otherwise
	 */
	public boolean isConnectionAvailable();

	/**
	 * @return SSC URL (including credentials), or null if not available/configured
	 */
	public String getSSCUrl();

}