// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * A bean to store Intersection data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Intersection extends NavigationDataBean {

	/**
	 * Creates a new Intersection object.
	 * @param lat the latitude in degrees
	 * @param lon the longitude in degrees
	 */
	public Intersection(double lat, double lon) {
		super(INT, lat, lon);
	}

	/**
	 * Returns the Intersection's name. <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always
	 */
	public final String getName() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Updates the Intersection's name. <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always
	 */
	public final void setName(String name) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Return the default Google Maps icon color.
	 * @return MapEntry#White
	 */
	public String getIconColor() {
		return WHITE;
	}

	/**
	 * Returns the default Google Maps infobox text.
	 * @return an HTML String
	 */
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<span class=\"mapInfoBox\">");
		buf.append(getHTMLTitle());
		buf.append(getHTMLPosition());
		buf.append("</span>");
		return buf.toString();
	}
}