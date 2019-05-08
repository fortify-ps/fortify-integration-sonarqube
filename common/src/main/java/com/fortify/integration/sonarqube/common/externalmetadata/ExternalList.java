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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.codehaus.staxmate.in.SMInputCursor;

// TODO This code can probably use some clean-up
public class ExternalList {
	private String id;
	private String name;
	private String description;
	private final Map<String, ExternalCategory> externalCategoriesByName = new LinkedHashMap<>();
	private final Map<String, Collection<ExternalCategory>> internalToExternalCategoryMapping = new LinkedHashMap<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Collection<ExternalCategory> getAllExternalCategories() {
		return externalCategoriesByName.values();
	}

	public Collection<ExternalCategory> getExternalCategoriesForFortifyCategory(String fortifyCategory) {
		return internalToExternalCategoryMapping.get(fortifyCategory);
	}

	public static final ExternalList parse(SMInputCursor childCursor) throws XMLStreamException {
		ExternalList result = new ExternalList();
		while (childCursor.getNext() != null) {
			String nodeName = childCursor.getLocalName();

			if ("ExternalListID".equals(nodeName)) {
				result.setId(StringUtils.trim(childCursor.collectDescendantText(false)));
			} else if ("Name".equals(nodeName)) {
				result.setName(StringUtils.trim(childCursor.collectDescendantText(false)));
			} else if ("Description".equals(nodeName)) {
				result.setDescription(StringUtils.trim(childCursor.collectDescendantText(false)));
			} else if ("ExternalCategoryDefinition".equals(nodeName)) {
				ExternalCategory category = ExternalCategory.parse(result, childCursor.childCursor());
				result.externalCategoriesByName.put(category.getName(), category);
			} else if ("Mapping".equals(nodeName)) {
				addMapping(result, childCursor.childCursor());
			}
		}
		return result;
	}
	
	private static final void addMapping(ExternalList externalList, SMInputCursor childCursor) throws XMLStreamException {
		String internalCategory = null;
		String externalCategory = null;
		while (childCursor.getNext() != null) {
			String nodeName = childCursor.getLocalName();
			if ("InternalCategory".equals(nodeName)) {
				internalCategory = StringUtils.trim(childCursor.collectDescendantText(false));
			} else if ("ExternalCategory".equals(nodeName)) {
				externalCategory = StringUtils.trim(childCursor.collectDescendantText(false));
			}
		}
		Collection<ExternalCategory> externalCategories = externalList.internalToExternalCategoryMapping.get(internalCategory);
		if ( externalCategories == null ) {
			externalCategories = new ArrayList<>();
			externalList.internalToExternalCategoryMapping.put(internalCategory, externalCategories);
		}
		externalCategories.add(externalList.externalCategoriesByName.get(externalCategory));
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
