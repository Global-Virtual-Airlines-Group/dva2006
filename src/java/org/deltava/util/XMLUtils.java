// Copyright (c) 2005 Delta Virtual Airlines. All Rights Reserved.
package org.deltava.util;

import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * A utility class for performing XML operations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class XMLUtils {

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
	 * Converts an XML document into text using UTF-8.
	 * @param doc the document
	 * @return the formatted XML document
	 */
	public static String format(Document doc) {
		return format(doc, "UTF-8");
	}
}