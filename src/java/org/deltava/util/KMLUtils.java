// Copyright 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;

import org.jdom.*;
import org.jdom.filter.ElementFilter;

import org.deltava.beans.GeospaceLocation;

/**
 * A utility class for performing Google Earth KML operations.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class KMLUtils extends XMLUtils {

	// Singleton constructor
	private KMLUtils() {
		super();
	}
	
	/**
	 * Creates a KML root element with the proper namespace and schema definitions.
	 * @return a KML kml element
	 */
	public static Document createKMLRoot() {
		Document doc = new Document();
		Element re = new Element("kml", Namespace.getNamespace("http://earth.google.com/kml/2.1"));
		re.setAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		re.setAttribute("schemaLocation", "http://earth.google.com/kml/2.1 http://code.google.com/apis/kml/schema/kml21.xsd");
		doc.setRootElement(re);
		return doc;
	}
	
	/**
	 * Updates the namespace of all elements in a KML document to match the parent namespace. This
	 * is a bit of a hack since we should probably set the namespace correctly when we create the document. 
	 * @param doc the KML document
	 */
	public static void copyNamespace(Document doc) {
		Element re = doc.getRootElement();
		for (Iterator i = re.getDescendants(new ElementFilter()); i.hasNext(); ) {
			Element e = (Element) i.next();
			if (e.getNamespace() != re.getNamespace())
				e.setNamespace(re.getNamespace());
		}
	}

	/**
	 * Sets the visibility for a particular XML element.
	 * @param e the KML element
	 * @param isVisible TRUE if the element is visible, otherwise FALSE
	 */
	public static void setVisibility(Element e, boolean isVisible) {
		setChildText(e, "visibility", isVisible ? "1" : "0");
	}

	/**
	 * Generates a KML icon element.
	 * @param palette the Google Earth pallete
	 * @param iconCode the icon code
	 * @return a KML Icon element
	 */
	public static Element createIcon(int palette, int iconCode) {
		Element e = new Element("Icon");
		e.addContent(createElement("href", "http://maps.google.com/mapfiles/kml/pal" + palette + "/icon" + iconCode + ".png", true));
		return e;
	}

	/**
	 * Generates a KML LookAt element, relative to an existing element.
	 * @param loc the location coordinates of the element
	 * @param altitude the altitude of the viewpoint
	 * @param heading the direction of the eyepoint
	 * @param tilt the view tilt angle
	 * @return a KML LookAt element
	 */
	public static Element createLookAt(GeospaceLocation loc, int altitude, int heading, int tilt) {
		Element e = new Element("LookAt");
		e.addContent(createElement("longitude", StringUtils.format(loc.getLongitude(), "##0.0000")));
		e.addContent(createElement("latitude", StringUtils.format(loc.getLatitude(), "##0.0000")));
		e.addContent(createElement("range", StringUtils.format(0.3048d * altitude, "##0.000")));
		e.addContent(createElement("heading", StringUtils.format(heading, "##0.00")));
		e.addContent(createElement("tilt", StringUtils.format(tilt, "##0.00")));
		return e;
	}
}