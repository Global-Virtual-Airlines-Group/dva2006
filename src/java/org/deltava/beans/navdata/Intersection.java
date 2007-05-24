// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
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
	
	/**
	 * Parses a North Atlantic Track latitude/longitude waypoint code.
	 * @param code the waypoint code
	 * @return an Intersection
	 * @throws IllegalArgumentException if the code is not in the format NN/WW or NNWWN
	 * @throws NullPointerException if code is null
	 * @throws NumberFormatException if the latitude/longitude cannot be parsed
	 */
	public static Intersection parseNAT(String code) {
		if (code.contains("/") && (code.length() == 5)) {
			return parseNAT(code.substring(0, 2) + code.substring(3) + "N");
		} else if (code.endsWith("N") && (code.length() == 5)) {
			double lat = Double.parseDouble(code.substring(0, 2));
			double lng = Double.parseDouble(code.substring(2, 4)) * -1;
			Intersection i = new Intersection(lat, lng);
			i.setCode(code);
			return i;
		} else
			throw new IllegalArgumentException("Invalid NAT waypoint - " + code);
	}
}