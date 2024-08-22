// Copyright 2005, 2006, 2008, 2012, 2021, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import org.jdom2.*;
import org.jdom2.output.*;

/**
 * A utility class for performing XML operations.
 * @author Luke
 * @version 11.2
 * @since 1.0
 */

public class XMLUtils {

	// singleton
	protected XMLUtils() {
		super();
	}
	
	/**
	 * Finds a child element multiple levels down and returns its value.
	 * @param e the Element
	 * @param names the ordered list of child names
	 * @return an Element, or null if not found
	 */
	public static String getChildText(Element e, String... names) {
		Element ce = e;
		for (String n : names) {
			ce = ce.getChild(n);
			if (ce == null) return null;
		}
		
		return ce.getTextTrim();
	}

	/**
	 * Creates a new XML document element.
	 * @param name the element name
	 * @param value the element value
	 * @param asCDATA TRUE if the value should be rendered as a CDATA, otherwise FALSE
	 * @return the XML element
	 */
	public static Element createElement(String name, String value, boolean asCDATA) {
		Element e = new Element(name);
		if (asCDATA)
			e.addContent(new CDATA(value));
		else
			e.setText(value);
		
		return e;
	}
	
	/**
	 * Creates a new XML document element.
	 * @param name the element name
	 * @param value the element value
	 * @return the XML element
	 */
	public static Element createElement(String name, String value) {
		return createElement(name, value, false);
	}

	/**
	 * Creates a new XML document element if the value is not null or empty.
	 * @param name the element name
	 * @param value the element value
	 * @return the XML element, or null
	 */
	public static Element createIfPresent(String name, String value) {
		return StringUtils.isEmpty(value) ? null : createElement(name, value, false);
	}
	
	/**
	 * Creates a new XML document element, with an embedded child element.
	 * @param name the element name
	 * @param subElementName the child element name
	 * @param value the child element value
	 * @return the element
	 */
	public static Element createElement(String name, String subElementName, String value) {
		Element e = new Element(name);
		Element e2 = new Element(subElementName);
		e2.setText(value);
		e.addContent(e2);
		return e;
	}
	
	/**
	 * Returns if an XML element contains a child element with a particular name.
	 * @param e the XML Element
	 * @param name the child name
	 * @return TRUE if a child element with the specified name exists, otherwise FALSE
	 */
	public static boolean hasElement(Element e, String name) {
		Element ce = e.getChild(name);
		return (ce != null);
	}
	
	/**
	 * Adds a child XML document element to an existing element, if it is not null 
	 * @param be the base element
	 * @param e the child element
	 * @return if e is not null
	 */
	public static boolean addIfPresent(Element be, Element e) {
		if (e != null) be.addContent(e);
		return (e != null);
	}
	
	/**
	 * Updates a child element's text. If the element does not exist, it will be created. 
	 * @param e the parent Element
	 * @param ceName the child element name
	 * @param value the child element value
	 */
	public static void setChildText(Element e, String ceName, String value) {
		Element ce = e.getChild(ceName);
		if (ce == null) {
			ce = createElement(ceName, value);
			ce.setNamespace(e.getNamespace());
			e.addContent(ce);
		} else
			ce.setText(value);
	}
	
	/**
	 * Converts an XML document into text using a specific character set.
	 * @param doc the document
	 * @param encoding the string encoding
	 * @return the formatted XML document
	 */
	public static String format(Document doc, String encoding) {
		XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat().setEncoding(encoding));
		return xmlOut.outputString(doc);
	}
	
	/**
	 * Removes invalid unicode characters from an XML element.
	 * @param txt the string to process
	 * @return the string with invalid unicode removed
	 */
	public static String stripInvalidUnicode(String txt) {
		if (txt == null)
			return null;
		
		StringBuilder buf = new StringBuilder();
		for (int x = 0; x < txt.length(); x++) {
			int c = txt.codePointAt(x);
			if (c == 0x12)
				buf.append('\'');
			else if (c > 0x1A)
				buf.append(txt.charAt(x));
		}
		
		return buf.toString();
	}
}