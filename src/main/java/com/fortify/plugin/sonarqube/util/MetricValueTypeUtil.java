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
package com.fortify.plugin.sonarqube.util;

import java.util.HashMap;
import java.util.Map;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.ValueType;

/**
 * This utility class allows for mapping SonarQube Metric ValueTypes to the corresponding 
 * Java type.
 *  
 * @author Ruud Senden
 *
 */
public final class MetricValueTypeUtil {
	private static final Map<Metric.ValueType, Class<?>> METRIC_VALUE_TYPE_TO_CLASS_MAP = getValueTypeToClassMap();
	
	private MetricValueTypeUtil() {}
	
	/**
	 * Define the {@link ValueType} to {@link Class} {@link Map}.
	 * This is based on the mapping defined in {@link ValueType},
	 * but since the mapping there is class-private, we cannot 
	 * re-use the mapping.
	 * @return
	 */
	private static final Map<Metric.ValueType, Class<?>> getValueTypeToClassMap() {
		Map<Metric.ValueType, Class<?>> result = new HashMap<Metric.ValueType, Class<?>>();
		result.put(Metric.ValueType.INT,Integer.class);
		result.put(Metric.ValueType.FLOAT,Double.class);
		result.put(Metric.ValueType.PERCENT,Double.class);
		result.put(Metric.ValueType.BOOL,Boolean.class);
		result.put(Metric.ValueType.STRING,String.class);
		result.put(Metric.ValueType.MILLISEC,Long.class);
		result.put(Metric.ValueType.DATA,String.class);
		result.put(Metric.ValueType.LEVEL,Metric.Level.class);
		result.put(Metric.ValueType.DISTRIB,String.class);
		result.put(Metric.ValueType.RATING,Integer.class);
		result.put(Metric.ValueType.WORK_DUR,Long.class);
		return result;
	}
	
	/**
	 * Get the Java {@link Class} that corresponds to the given SonarQube {@link ValueType}.
	 * @param valueType
	 * @return
	 */
	public static final Class<?> getValueTypeClass(Metric.ValueType valueType) {
		return METRIC_VALUE_TYPE_TO_CLASS_MAP.get(valueType);
	}
	
	/**
	 * Get the Java {@link Class} that corresponds to the {@link ValueType} defined for the 
	 * given SonarQube {@link Metric}.
	 * 
	 * @param metric
	 * @return
	 */
	public static final Class<?> getValueTypeClass(Metric<?> metric) {
		return getValueTypeClass(metric.getType());
	}
}
