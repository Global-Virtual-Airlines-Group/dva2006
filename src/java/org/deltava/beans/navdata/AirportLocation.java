// Copyright 2005, 2006, 2008, 2012, 2014, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import org.deltava.beans.schedule.*;
import org.deltava.util.StringUtils;

/**
 * A class to store airport location data.
 * @author Luke
 * @version 5.5
 * @since 1.0
 */

public class AirportLocation extends NavigationDataBean implements ICAOAirport {

	private int _altitude;
	private double _magVar;

	/**
	 * Creates a new Airport location object.
	 * @param lat the latitude in degrees
	 * @param lon the longitude in degrees
	 */
	public AirportLocation(double lat, double lon) {
		super(Navaid.AIRPORT, lat, lon);
	}
	
	/**
	 * Creates a Airport location from an Airport bean.
	 * @param a the Airport bean
	 */
	public AirportLocation(Airport a) {
		super(Navaid.AIRPORT, a.getLatitude(), a.getLongitude());
		_altitude = a.getAltitude();
		setCode(a.getICAO());
		setName(a.getName());
		setAltitude(a.getAltitude());
		setRegion(a.getRegion());
		_magVar = a.getMagVar();
	}
	
	/**
	 * Returns the Airport's ICAO code.
	 */
	@Override
	public String getICAO() {
		return getCode();
	}

	/**
	 * Returns the Airport's altitude.
	 * @return the altitude in feet above mean sea level
	 */
	@Override
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
	 * Updates the airport's magnetic variation.
	 * @param var the variation in degrees
	 */
	public void setMagVar(double var) {
		_magVar = Math.min(50, Math.max(-50, var));
	}
	
	/**
	 * Returns the airway code.
	 */
	@Override
	public final String getAirway() {
		return null;
	}

	@Override
	public double getMagVar() {
		return _magVar;
	}

	/**
	 * Return the default Google Maps icon color.
	 * @return MapEntry.GREEN
	 */
	@Override
	public String getIconColor() {
		return GREEN;
	}
	
	/**
	 * Returns the Google Earth palette code.
	 * @return 2
	 */
	@Override
	public int getPaletteCode() {
		return 2;
	}
	
	/**
	 * Returns the Google Earth icon code.
	 * @return 48
	 */
	@Override
	public int getIconCode() {
		return 48;
	}

	/**
	 * Returns the default Google Maps infobox text.
	 * @return an HTML String
	 */
	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox navdata\">");
		buf.append(getHTMLTitle());
		buf.append(getHTMLPosition());
		buf.append("Altitude: ");
		buf.append(StringUtils.format(_altitude, "#,##0"));
		buf.append(" feet MSL");
		if (Math.abs(_magVar) > 0.01) {
			buf.append("<br />Magnetic variation: ");
			buf.append(StringUtils.format(_magVar, "#0.00"));
			buf.append(" degrees");
		}
		
		buf.append("</div>");
		return buf.toString();
	}
}