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
package com.fortify.integration.sonarqube.common.externalmetadata;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;

public class FortifyExternalMetadata {
	private final Map<String, ExternalList> externalLists;
	
	private FortifyExternalMetadata(Map<String, ExternalList> externalLists) {
		this.externalLists = Collections.unmodifiableMap(externalLists);
	}
	
	public ExternalList getExternalListByName(String name) {
		return externalLists.get(name);
	}
	
	public Collection<String> getExternalListNames() {
		return externalLists.keySet();
	}

	public static final FortifyExternalMetadata parse() {
		try {
			URL url = FortifyExternalMetadata.class.getClassLoader().getResource("externalmetadata.xml");
			if ( url == null ) { return null; }
			return parse(url);
		} catch (Exception e) {
			throw new RuntimeException("Unexpected error during the parse of externalmetadata.xml", e);
		}
	}

	public static final FortifyExternalMetadata parse(URL url) throws XMLStreamException {
		Map<String, ExternalList> map = new LinkedHashMap<>();
		SMHierarchicCursor rootC = getInputFactory().rootElementCursor(url);
		rootC.advance(); // <ExternalMetadataPack>

		SMInputCursor externalMetadataPackCursor = rootC.childCursor();

		while (externalMetadataPackCursor.getNext() != null) {
			if ("ExternalList".equals(externalMetadataPackCursor.getLocalName())) {
				ExternalList externalList = ExternalList.parse(externalMetadataPackCursor.childCursor());
				map.put(externalList.getName(), externalList);
			}
		}
		return new FortifyExternalMetadata(map);
	}

	private static final SMInputFactory getInputFactory() throws FactoryConfigurationError {
		XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
	    xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
	    xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
	    // just so it won't try to load DTD in if there's DOCTYPE
	    xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
	    xmlFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
	    return new SMInputFactory(xmlFactory);
	}
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}