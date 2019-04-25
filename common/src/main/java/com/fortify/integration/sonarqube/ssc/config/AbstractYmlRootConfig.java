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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public class AbstractYmlRootConfig extends AbstractYmlConfig {
	public static final <T extends AbstractYmlRootConfig> T load(String name, Class<T> type) {
		URL url = AbstractYmlRootConfig.class.getClassLoader().getResource(name);
		if ( url == null ) {
			throw new RuntimeException("File "+name+" cannot be found on the class path");
		}
		return load(url, type);
	}
	
	public static final <T extends AbstractYmlRootConfig> T load(Path path, Class<T> type) {
		try {
			return load(path.toUri().toURL(), type);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Error loading Yaml file from "+path.toString(), e);
		}
	}
	
	public static final <T extends AbstractYmlRootConfig> T load(URL url, Class<T> type) {
		try {
			return MAPPER.readValue(url, type);
		} catch (IOException e) {
			throw new RuntimeException("Error loading Yaml file from "+url.toString(), e);
		}
	}
	
	public static final <T extends AbstractYmlRootConfig> T create(Class<T> type) {
		try {
			return type.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Error creating new instance for "+type.getName());
		}
	}
	
	public void save(Path outputPath) {
		save(outputPath.toFile());
	}
	
	public void save(File outputFile) {
		try {
			MAPPER.writeValue(outputFile, this);
		} catch (IOException e) {
			throw new RuntimeException("Error saving Yaml file to "+outputFile.toString(), e);
		}
	}
}
