// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import org.deltava.beans.GeospaceLocation;

import org.deltava.util.StringUtils;

/**
 * A class to store airport location data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirportLocation extends NavigationDataBean implements GeospaceLocation {

	private int _altitude;

	/**
	 * Creates a new Airport location object.
	 * @param lat
	 * @param lon
	 */
	public AirportLocation(double lat, double lon) {
		super(AIRPORT, lat, lon);
	}

	/**
	 * Returns the Airport's altitude.
	 * @return the altitude in feet above mean sea level
	 */
	public int getAltitude() {
		return _altitude;
	}

	/**
	 * Updates the Airport's altitude.
	 * @param alt the altitude in feet above mean sea level
	 * @throws IllegalArgumentException if alt < -1500 or > 29000
	 */
	public void setAltitude(int alt) {
		if ((alt < -1500) || (alt > 29000))
			throw new IllegalArgumentException("Altitude cannot be < -1500 or > 29000");

		_altitude = alt;
	}

	/**
	 * Return the default Google Maps icon color.
	 * @return org.deltava.beans.MapEntry.GREEN
	 */
	public String getIconColor() {
		return GREEN;
	}

	/**
	 * Returns the default Google Maps infobox text.
	 * @return an HTML String
	 */
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<span class=\"mapInfoBox\">");
		buf.append(getHTMLTitle());
		buf.append(getHTMLPosition());
		buf.append("Altitude: ");
		buf.append(StringUtils.format(_altitude, "#,##0"));
		buf.append(" feet MSL</span>");
		return buf.toString();
	}
}