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
package com.fortify.integration.sonarqube.common.source.ssc.metrics;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

@SuppressWarnings("rawtypes")
public class FortifySSCConnectionPropertiesMetrics implements Metrics {
	public static final String PRP_SSC_URL = "fortify.ssc.url";
	public static final String PRP_APP_VERSION_ID = "fortify.ssc.applicationVersionId";
	public static final String PRP_FILTER_SET_GUID = "fortify.ssc.filterSetGuid";
	
	public static final Metric METRIC_SSC_URL = new Metric.Builder(PRP_SSC_URL, "SSC URL", Metric.ValueType.STRING)
			.setDomain("Fortify SSC").setHidden(true).create();
	public static final Metric METRIC_SSC_APP_VERSION_ID = new Metric.Builder(PRP_APP_VERSION_ID, "SSC Application Version Id", Metric.ValueType.STRING)
			.setDomain("Fortify SSC").setHidden(true).create();
	public static final Metric METRIC_SSC_FILTER_SET_GUID = new Metric.Builder(PRP_FILTER_SET_GUID, "SSC Filter Set Guid", Metric.ValueType.STRING)
			.setDomain("Fortify SSC").setHidden(true).create();
	
	public static final String[] METRICS_KEYS = {PRP_SSC_URL, PRP_APP_VERSION_ID, PRP_FILTER_SET_GUID};
	private static final List<Metric> METRICS = Arrays.asList(new Metric[] {METRIC_SSC_URL, METRIC_SSC_APP_VERSION_ID, METRIC_SSC_FILTER_SET_GUID});
	
	@Override
	public List<Metric> getMetrics() {
		return METRICS;
	}
}
