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
package com.fortify.integration.sonarqube.common.config;

import java.util.ArrayList;
import java.util.List;

import com.fortify.integration.sonarqube.common.SourceSystem;

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
	
	public static final MetricsConfig load(SourceSystem sourceSystem) {
		return load(getMetricsConfigYmlName(sourceSystem), MetricsConfig.class);
	}
	
	public static final String getMetricsConfigYmlName(SourceSystem sourceSystem) {
		return "metrics-"+sourceSystem.id()+".yml";
	}

	public List<MetricConfig> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<MetricConfig> metrics) {
		this.metrics = metrics;
	}
	
	public void addMetricConfig(MetricConfig metricConfig) {
		List<MetricConfig> oldValue = this.metrics;
		this.metrics = new ArrayList<>(this.metrics);
		this.metrics.add(metricConfig);
		propertyChangeSupport.firePropertyChange("metrics", oldValue, this.metrics);
	}
	
	public void removeMetricConfig(MetricConfig metricConfig) {
		List<MetricConfig> oldValue = this.metrics;
		this.metrics = new ArrayList<>(this.metrics);
		this.metrics.remove(metricConfig);
		propertyChangeSupport.firePropertyChange("metrics", oldValue, this.metrics);
	}

	
	public static final class MetricConfig extends AbstractYmlConfig {
		private String key = "newKey";
		private String name = "New Metric";
		private String domain = "Fortify";
		private String description = "No Description";
		private MetricValueType type = MetricValueType.STRING;
		private boolean qualitative = false;
		private Direction direction = Direction.NONE;
		private String expr;
		
		public String getKey() {
			return key;
		}
		public void setKey(String newValue) {
			String oldValue = this.key;
			this.key = newValue;
			propertyChangeSupport.firePropertyChange("key", oldValue, newValue);
		}
		public String getName() {
			return name;
		}
		public void setName(String newValue) {
			String oldValue = this.name;
			this.name = newValue;
			propertyChangeSupport.firePropertyChange("name", oldValue, newValue);
		}
		public String getDomain() {
			return domain;
		}
		public void setDomain(String newValue) {
			String oldValue = this.domain;
			this.domain = newValue;
			propertyChangeSupport.firePropertyChange("domain", oldValue, newValue);
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String newValue) {
			String oldValue = this.description;
			this.description = newValue;
			propertyChangeSupport.firePropertyChange("description", oldValue, newValue);
		}
		public MetricValueType getType() {
			return type;
		}
		public void setType(MetricValueType newValue) {
			MetricValueType oldValue = this.type;
			this.type = newValue;
			propertyChangeSupport.firePropertyChange("type", oldValue, newValue);
		}
		public boolean isQualitative() {
			return qualitative;
		}
		public void setQualitative(boolean newValue) {
			boolean oldValue = this.qualitative;
			this.qualitative = newValue;
			propertyChangeSupport.firePropertyChange("qualitative", oldValue, newValue);
		}
		public Direction getDirection() {
			return direction;
		}
		public void setDirection(Direction newValue) {
			Direction oldValue = this.direction;
			this.direction = newValue;
			propertyChangeSupport.firePropertyChange("direction", oldValue, newValue);
		}
		public String getExpr() {
			return expr;
		}
		public void setExpr(String newValue) {
			String oldValue = this.expr;
			this.expr = newValue;
			propertyChangeSupport.firePropertyChange("expr", oldValue, newValue);
		}
	}
}
