// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import org.deltava.beans.GeospaceLocation;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.StringUtils;

/**
 * A class to store airport location data.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class AirportLocation extends NavigationDataBean implements GeospaceLocation {

	private int _altitude;

	/**
	 * Creates a new Airport location object.
	 * @param lat the latitude in degrees
	 * @param lon the longitude in degrees
	 */
	public AirportLocation(double lat, double lon) {
		super(AIRPORT, lat, lon);
	}
	
	/**
	 * Creates a Airport location from an Airport bean.
	 * @param a the Airport bean
	 */
	public AirportLocation(Airport a) {
		super(AIRPORT, a.getLatitude(), a.getLongitude());
		_altitude = a.getAltitude();
		setCode(a.getICAO());
		setName(a.getName());
		setAltitude(a.getAltitude());
		setRegion(a.getRegion());
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
	 * Returns the airway code.
	 */
	@Override
	public final String getAirway() {
		return null;
	}

	/**
	 * Return the default Google Maps icon color.
	 * @return MapEntry.GREEN
	 */
	public String getIconColor() {
		return GREEN;
	}
	
	/**
	 * Returns the Google Earth palette code.
	 * @return 2
	 */
	public int getPaletteCode() {
		return 2;
	}
	
	/**
	 * Returns the Google Earth icon code.
	 * @return 48
	 */
	public int getIconCode() {
		return 48;
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