// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import org.jdom.Element;

import org.deltava.beans.GeospaceLocation;

/**
 * A utility class for performing Google Earth KML operations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class KMLUtils extends XMLUtils {

	// Singleton constructor
	private KMLUtils() {
		super();
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
	 * @param pX the icon x-offset in the palette
	 * @param pY the icon y-offset in the palette
	 * @return a KML IconStyle element
	 */
	public static Element createIcon(int palette, int pX, int pY) {
		Element re = new Element("IconStyle");
		Element e = new Element("Icon");
		e.addContent(createElement("href", "root://icons/palette-" + palette + ".png", true));
		e.addContent(createElement("x", String.valueOf(pX * 32)));
		e.addContent(createElement("y", String.valueOf(pY * 32)));
		e.addContent(createElement("w", "32"));
		e.addContent(createElement("h", "32"));
		re.addContent(e);
		return re;
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
		e.addContent(XMLUtils.createElement("longitude", StringUtils.format(loc.getLongitude(), "##0.0000")));
		e.addContent(XMLUtils.createElement("latitude", StringUtils.format(loc.getLatitude(), "##0.0000")));
		e.addContent(XMLUtils.createElement("range", StringUtils.format(0.3048d * altitude, "##0.000")));
		e.addContent(XMLUtils.createElement("heading", StringUtils.format(heading, "##0.00")));
		e.addContent(XMLUtils.createElement("tilt", StringUtils.format(tilt, "##0.00")));
		return e;
	}
}