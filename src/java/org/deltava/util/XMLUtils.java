// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * A utility class for performing XML operations.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class XMLUtils {

	/**
	 * Private singleton constructor. <i>Not implemented</i>
	 */ 
	protected XMLUtils() {
		super();
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
	 * @see XMLUtils#format(Document)
	 */
	public static String format(Document doc, String encoding) {
		XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat().setEncoding(encoding));
		return xmlOut.outputString(doc);
	}
	
	/**
	 * Converts an XML document into text using UTF-8.
	 * @param doc the document
	 * @return the formatted XML document
	 * @see XMLUtils#format(Document, String)
	 */
	public static String format(Document doc) {
		return format(doc, "UTF-8");
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