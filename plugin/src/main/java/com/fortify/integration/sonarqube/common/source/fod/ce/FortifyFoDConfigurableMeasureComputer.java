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
package com.fortify.integration.sonarqube.common.source.fod.ce;

import com.fortify.client.fod.api.FoDReleaseAPI;
import com.fortify.client.fod.connection.FoDAuthenticatingRestConnection;
import com.fortify.integration.sonarqube.common.ce.AbstractFortifyConfigurableMeasureComputer;
import com.fortify.integration.sonarqube.common.source.fod.metrics.FortifyFoDConfigurableMetrics;
import com.fortify.util.rest.json.JSONMap;

public final class FortifyFoDConfigurableMeasureComputer extends AbstractFortifyConfigurableMeasureComputer<FortifyFoDComputeEngineSideConnectionHelper> {
	public FortifyFoDConfigurableMeasureComputer() {
		super(FortifyFoDConfigurableMetrics.METRICS_CONFIG);
	}
	
	@Override
	protected final String[] getConnectionPropertiesMetricKeys() {
		return FortifyFoDComputeEngineSideConnectionHelper.getInputMetricKeys();
	}
	
	@Override
	protected FortifyFoDComputeEngineSideConnectionHelper getComputeEngineSideConnectionHelper(MeasureComputerContext context) {
		return new FortifyFoDComputeEngineSideConnectionHelper(context);
	}
	
	/**
	 * This method retrieves release data from FoD. This data includes
	 * the standard release JSON fields, as well as various on-demand
	 * fields that provide additional data that can be used in metric 
	 * calculations.
	 *  
	 * @param connHelper
	 * @return
	 */
	protected final JSONMap getConfigurableMeasuresInputData(FortifyFoDComputeEngineSideConnectionHelper connHelper) {
		FoDAuthenticatingRestConnection conn = connHelper.getConnection();
		String releaseId = connHelper.getReleaseId();
		JSONMap release = conn.api(FoDReleaseAPI.class).queryReleases()
				.releaseId(releaseId)
				// TODO Any other interesting API's that we could load on demand?
				.build().getUnique();
		if ( release==null ) {
			throw new IllegalArgumentException("FoD release "+releaseId+" not found");
		}
		return release;
	}
}
