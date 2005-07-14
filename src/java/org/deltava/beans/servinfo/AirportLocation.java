// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.servinfo;

import org.deltava.beans.schedule.Airport;

/**
 * A lightweight bean to store ICAO/location data about an Airport.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirportLocation {

	private String _icao;
	private double _latitude;
	private double _longitude;
	
	/**
	 * Creates a new airport location bean.
	 * @param icao the ICAO code
	 * @throws NullPointerException if icao is null  
	 */
	public AirportLocation(String icao) {
		super();
		_icao = icao.toUpperCase();
	}

	/**
	 * Creates an airport location bean from a Schedule airport.
	 * @param a the Airport bean
	 */
	public AirportLocation(Airport a) {
		this(a.getICAO());
		setLocation(a.getLatitude(), a.getLongitude());
	}
	
	/**
	 * Sets this airport's location. Unlike {@link Airport#setLocation(double, double)}, no validation takes place.
	 * @param lat This airport's latitude
	 * @param lng This airport's longitude
	 */
	public void setLocation(double lat, double lng) {
		_latitude = lat;
		_longitude = lng;
	}
	
	/**
	 * Returns this airport's ICAO code.
	 * @return the ICAO code
	 */
	public String getICAO() {
		return _icao;
	}
	
	/**
	 * Returns this airport's latitude in degrees. Latitudes south of the Equator are negative.
	 * @return the latitude in degrees
	 */
	public double getLatitude() {
		return _latitude;
	}
	
	/**
	 * Returns this airport's longtitude in degrees. Longtitudes west of the Greenwich Meridian are negative.
	 * @return the longtitude in degrees
	 */
	public double getLongitude() {
		return _longitude;
	}
}