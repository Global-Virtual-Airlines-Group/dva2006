// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import org.deltava.util.StringUtils;

/**
 * A bean to store runway information.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class Runway extends NavigationFrequencyBean {

	private int _length;
	private int _heading;

	/**
	 * Creates a new Runway bean.
	 * @param lat the latitude of the start of the runway
	 * @param lon the longitude of the start of the runway
	 */
	public Runway(double lat, double lon) {
		super(RUNWAY, lat, lon);
	}

	/**
	 * Returns the length of the runway.
	 * @return the length in feet
	 * @see Runway#setLength(int)
	 */
	public int getLength() {
		return _length;
	}

	/**
	 * Returns the runway heading.
	 * @return the heading in degrees
	 * @see Runway#setHeading(int)
	 */
	public int getHeading() {
		return _heading;
	}

	/**
	 * Updates the length of the runway.
	 * @param len the length in feet
	 * @throws IllegalArgumentException if len is zero, negative or > 25000
	 * @see Runway#getLength()
	 */
	public void setLength(int len) {
		if ((len < 1) || (len > 25000))
			throw new IllegalArgumentException("Length cannot be < 1 or > 25000");

		_length = len;
	}

	/**
	 * Updates the runway heading.
	 * @param hdg the heading in degrees
	 * @throws IllegalArgumentException if hdg is negative or > 360
	 * @see Runway#getHeading()
	 */
	public void setHeading(int hdg) {
		while (hdg > 360)
			hdg -= 360;
		if (hdg < 0)
			throw new IllegalArgumentException("Invalid Heading - " + hdg);

		_heading = hdg;
	}

	/**
	 * Return the default Google Maps icon color.
	 * @return org.deltava.beans.MapEntry.YELLOW
	 */
	public String getIconColor() {
		return YELLOW;
	}
	
	/**
	 * Returns the Google Earth palette code.
	 * @return 3
	 */
	public int getPaletteCode() {
		return 3;
	}
	
	/**
	 * Returns the Google Earth icon code.
	 * @return 60
	 */
	public int getIconCode() {
		return 60;
	}

	/**
	 * Returns the default Google Maps infobox text.
	 * @return an HTML String
	 */
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<span class=\"mapInfoBox\">");
		buf.append(getHTMLTitle());
		buf.append("Heading: ");
		buf.append(StringUtils.format(_heading, "000"));
		buf.append("<br />Length:");
		buf.append(StringUtils.format(_length, "#,##0"));

		// Add ILS frequency if found
		if (getFrequency() != null) {
			buf.append("<br />ILS Frequency: ");
			buf.append(getFrequency());
			buf.append("<br />");
		}

		buf.append(getHTMLPosition());
		buf.append("</span>");
		return buf.toString();
	}
}