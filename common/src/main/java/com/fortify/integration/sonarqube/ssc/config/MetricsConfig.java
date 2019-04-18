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
package com.fortify.integration.sonarqube.ssc.config;

import java.util.ArrayList;
import java.util.List;

public class MetricsConfig extends AbstractYmlRootConfig {
	public static enum MetricValueType {
		INT(Integer.class), 
		FLOAT(Double.class), 
		PERCENT(Double.class), 
		BOOL(Boolean.class), 
		STRING(String.class), 
		MILLISEC(Long.class), 
		DATA(String.class), 
		DISTRIB(String.class), 
		RATING(Integer.class), 
		WORK_DUR(Long.class);

		private final Class<?> valueClass;

		MetricValueType(Class<?> valueClass) {
			this.valueClass = valueClass;
		}

		public Class<?> valueType() {
			return valueClass;
		}

		public static String[] names() {
			MetricValueType[] values = values();
			String[] names = new String[values.length];
			for (int i = 0; i < values.length; i += 1) {
				names[i] = values[i].name();
			}

			return names;
		}
	}
	
	public static enum Direction {
		NONE(0), 
		WORST(-1), 
		BETTER(1);
		
		private final int direction;

		Direction(int direction) {
			this.direction = direction;
		}

		public int intValue() {
			return direction;
		}

		public static String[] names() {
			Direction[] values = values();
			String[] names = new String[values.length];
			for (int i = 0; i < values.length; i += 1) {
				names[i] = values[i].name();
			}
			return names;
		}
	}
	
	
	private List<MetricConfig> metrics = new ArrayList<>();
	
	public static final MetricsConfig load() {
		return load("metrics.yml", MetricsConfig.class);
	}

	public List<MetricConfig> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<MetricConfig> metrics) {
		this.metrics = metrics;
	}

	
	public static final class MetricConfig extends AbstractYmlConfig {
		private String key;
		private String name;
		private String domain = "Fortify";
		private String description = "Custom metric";
		private MetricValueType type = MetricValueType.STRING;
		private boolean qualitative = false;
		private Direction direction = Direction.NONE;
		private String expr;
		
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDomain() {
			return domain;
		}
		public void setDomain(String domain) {
			this.domain = domain;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public MetricValueType getType() {
			return type;
		}
		public void setType(MetricValueType type) {
			this.type = type;
		}
		public boolean isQualitative() {
			return qualitative;
		}
		public void setQualitative(boolean qualitative) {
			this.qualitative = qualitative;
		}
		public Direction getDirection() {
			return direction;
		}
		public void setDirection(Direction direction) {
			this.direction = direction;
		}
		public String getExpr() {
			return expr;
		}
		public void setExpr(String expr) {
			this.expr = expr;
		}
	}
}
