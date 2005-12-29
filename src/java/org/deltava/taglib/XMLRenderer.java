// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib;

import java.util.*;

/**
 * A helper class to generate XML elements with attributes.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class XMLRenderer {
	
	private String _name;
	private Map<String, String> _attrs;

	/**
	 * Creates a new XML element, with a lowercase name.
	 * @param name the element nam
	 * @throws NullPointerException if name is null
	 */
	public XMLRenderer(String name) {
		super();
		_name = name.toLowerCase();
		_attrs = new LinkedHashMap<String, String>();
	}
	
	/**
	 * Returns the XML element name.
	 * @return the element name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns wether the element has a particular attribute.
	 * @param name the attribute name
	 * @return TRUE if the attribute exists, otherwise FALSE
	 */
	public boolean hasElement(String name) {
		return _attrs.containsKey(name.toLowerCase());
	}
	
	/**
	 * Clears all element attributes. 
	 */
	public void clear() {
		_attrs.clear();
	}

	/**
	 * Sets an element attribute.
	 * @param name the attribute name
	 * @param value the attribute value
	 */
	public void setAttribute(String name, String value) {
		_attrs.put(name.toLowerCase(), value);
	}
	
	/**
	 * Renders the open element tag as XML.
	 * @param completeTag TRUE if the tag should be completed, otherwise FALSE
	 * @param finishTag TRUE if the tag should be finished, otherwise FALSE
	 * @return the element rendered to XML
	 */
	public String open(boolean completeTag, boolean finishTag) {
		StringBuilder buf = new StringBuilder();
		
		// Start with the element name
		buf.append('<');
		buf.append(_name);

		// Append a space if we have attributes
		if (_attrs.size() > 0)
			buf.append(' ');

		// Loop through the attributes
		for (Iterator<String> i = _attrs.keySet().iterator(); i.hasNext();) {
			String attrName = i.next();
			String attrValue = _attrs.get(attrName);

			// Append the attribute name and value
			buf.append(attrName);
			buf.append("=\"");
			buf.append(attrValue);
			buf.append('\"');

			// If there's another attribute, add a space
			if (i.hasNext())
				buf.append(' ');
		}

		// Close the tag if requested
		if (completeTag) {
			if (finishTag) {
				if (!_attrs.isEmpty())
					buf.append(' ');
				
				buf.append('/');
			}
			
			buf.append('>');
		}

		// Return the HTML
		return buf.toString();
	}
	
	/**
	 * Renders the open element tag as XML.
	 * @param completeTag TRUE if the tag should be completed, otherwise FALSE
	 * @return the element rendered to XML
	 */
	public String open(boolean completeTag) {
		return open(completeTag, false);
	}
	
	/**
	 * Renders the close element tag as XML.
	 * @return the close tag
	 */
	public String close() {
		StringBuilder buf = new StringBuilder();
		buf.append("</");
		buf.append(_name);
		buf.append('>');
		return buf.toString();
	}
}