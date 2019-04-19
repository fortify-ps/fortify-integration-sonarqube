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
package com.fortify.integration.sonarqube.ssc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;

import com.fortify.integration.sonarqube.ssc.metric.FortifyMetrics;
import com.fortify.integration.sonarqube.ssc.metric.FortifyMetricsSensor;
import com.fortify.integration.sonarqube.ssc.metric.provider.FortifyConfigurableMetricsProvider;
import com.fortify.integration.sonarqube.ssc.rule.FortifyRulesDefinition;

/**
 * This main plugin class adds all relevant SonarQube extension points that make
 * up this Fortify integration.
 */
public class FortifySSCPlugin implements Plugin {
	private static Class<?>[] extensions = { 
			FortifySSCConnectionFactory.class,
			FortifyRulesDefinition.class, 
			FortifyLanguage.class,
			FortifyProfile.class, 
			FortifyConfigurableMetricsProvider.class,
			FortifyMetrics.class,
			FortifyMetricsSensor.class};

	@Override
	public void define(Context context) {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		for (Class<?> extension : extensions) {
			context.addExtension(extension);
			invokePropertyDefinitionsMethod(extension, propertyDefinitions);
		}
		context.addExtensions(propertyDefinitions);
	}

	private static final void invokePropertyDefinitionsMethod(Class<?> type, List<PropertyDefinition> propertyDefinitions) {
		try {
			Method method = type.getDeclaredMethod("addPropertyDefinitions", List.class);
			if ((method.getModifiers() & Modifier.STATIC) != 0) {
				method.invoke(null, propertyDefinitions);
			}
		} catch (NoSuchMethodException e) {
			// Expected exception, as not all extension classes define the addPropertyDefinitions method
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException("Error adding property definitions from "+type.getName(), e);
		}
	}

}
