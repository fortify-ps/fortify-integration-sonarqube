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
package com.fortify.integration.sonarqube.common;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * This AspectJ {@link Aspect} sets the context class loader for all plugin methods and constructors
 * that are directly invoked from SonarQube, and restores the original class loader afterwards.
 * 
 * @author Ruud Senden
 *
 */
@Aspect
public class ContextClassLoaderAspect {
	private static final Logger LOG = Loggers.get(ContextClassLoaderAspect.class);
	
	/**
	 * {@link Pointcut} matching all constructors on all classes that extend from a class that has
	 * a SonarQube API annotation
	 */
	@Pointcut("execution(public ((@org.sonar.api..*.* *)+).new(..))")
	public void extensionPointConstructors() {
	}
	
	/**
	 * {@link Pointcut} matching all implementations of SonarQube API methods
	 */
	@Pointcut("execution(* org.sonar.api..*.*(..))")
	public void sonarQubePluginMethods() {
	}
	
	/**
	 * Call {@link #wrapWithContextClassLoader(ProceedingJoinPoint, String)} for
	 * any constructor matching {@link #extensionPointConstructors()}.
	 * @param joinPoint
	 * @return
	 * @throws Throwable
	 */
	@Around("extensionPointConstructors()")
	public Object wrapConstructorWithContextClassLoader(ProceedingJoinPoint joinPoint) throws Throwable {
		return wrapWithContextClassLoader(joinPoint, "constructor");
	}
	
	/**
	 * Call {@link #wrapWithContextClassLoader(ProceedingJoinPoint, String)} for
	 * any method matching {@link #sonarQubePluginMethods()()}.
	 * @param joinPoint
	 * @return
	 * @throws Throwable
	 */
	@Around("sonarQubePluginMethods()")
	public Object wrapMethodWithContextClassLoader(ProceedingJoinPoint joinPoint) throws Throwable {
		return wrapWithContextClassLoader(joinPoint, "method");
	}
	
	/**
	 * This method calls {@link #updateContextClassLoader(ProceedingJoinPoint, String, String, ClassLoader, ClassLoader)}
	 * to update the context class loader, then invokes the original method, and finally restores the original
	 * context class loader by again calling {@link #updateContextClassLoader(ProceedingJoinPoint, String, String, ClassLoader, ClassLoader)}
	 * @param joinPoint
	 * @param methodType
	 * @return
	 * @throws Throwable
	 */
	private Object wrapWithContextClassLoader(ProceedingJoinPoint joinPoint, String methodType) throws Throwable {
		ClassLoader orgContextCl = Thread.currentThread().getContextClassLoader();
		ClassLoader clazzCl = ContextClassLoaderAspect.class.getClassLoader();
    	try {
    		updateContextClassLoader(joinPoint, methodType, "Start", orgContextCl, clazzCl);
	        return joinPoint.proceed();
    	} finally {
    		updateContextClassLoader(joinPoint, methodType, "End", clazzCl, orgContextCl);
    	}
	}

	/**
	 * This method checks whether the given oldCl is different from the given newCl, and if so, sets the
	 * context class loader to newCl. If debug logging is enabled, this method will also log relevant
	 * debugging information.
	 * @param joinPoint
	 * @param methodType
	 * @param startOrEnd
	 * @param oldCl
	 * @param newCl
	 */
	private void updateContextClassLoader(ProceedingJoinPoint joinPoint, String methodType, String startOrEnd, ClassLoader oldCl, ClassLoader newCl) {
		if ( oldCl != newCl ) {
			if ( LOG.isDebugEnabled() ) {
				LOG.debug(startOrEnd+" "+methodType+" "+joinPoint.getSignature().toLongString()+": set context class loader to: "+newCl);
			}
			Thread.currentThread().setContextClassLoader(newCl);
		}
	}
}
