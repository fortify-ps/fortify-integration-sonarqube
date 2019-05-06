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
package com.fortify.integration.sonarqube.common.source.fod.metrics;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

@SuppressWarnings("rawtypes")
public class FortifyFoDConnectionPropertiesMetrics implements Metrics {
	public static final String PRP_FOD_URL = "fortify.fod.url";
	public static final String PRP_FOD_TENANT = "fortify.fod.tenant";
	public static final String PRP_FOD_USER = "fortify.fod.user";
	public static final String PRP_FOD_PWD = "fortify.fod.pwd";
	public static final String PRP_FOD_RELEASE_ID = "fortify.fod.releaseId";
	
	public static final Metric METRIC_FOD_URL = new Metric.Builder(PRP_FOD_URL, "FoD URL", Metric.ValueType.STRING)
			.setDomain("Fortify").setHidden(true).create();
	public static final Metric METRIC_FOD_TENANT = new Metric.Builder(PRP_FOD_TENANT, "FoD Tenant", Metric.ValueType.STRING)
			.setDomain("Fortify").setHidden(true).create();
	public static final Metric METRIC_FOD_USER = new Metric.Builder(PRP_FOD_USER, "FoD User", Metric.ValueType.STRING)
			.setDomain("Fortify").setHidden(true).create();
	public static final Metric METRIC_FOD_PWD = new Metric.Builder(PRP_FOD_PWD, "FoD Password", Metric.ValueType.STRING)
			.setDomain("Fortify").setHidden(true).create();
	
	public static final Metric METRIC_FOD_RELEASE_ID = new Metric.Builder(PRP_FOD_RELEASE_ID, "FoD Release Id", Metric.ValueType.STRING)
			.setDomain("Fortify").setHidden(true).create();
	
	public static final String[] METRICS_KEYS = {PRP_FOD_URL, PRP_FOD_TENANT, PRP_FOD_USER, PRP_FOD_PWD, PRP_FOD_RELEASE_ID};
	private static final List<Metric> METRICS = Arrays.asList(new Metric[] {METRIC_FOD_URL, METRIC_FOD_TENANT, METRIC_FOD_USER, METRIC_FOD_PWD, METRIC_FOD_RELEASE_ID});
	
	@Override
	public List<Metric> getMetrics() {
		return METRICS;
	}
}
