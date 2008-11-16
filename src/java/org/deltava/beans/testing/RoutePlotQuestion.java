// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.*;

/**
 * A bean to store route plotting Examination questions.
 * @author Luke
 * @version 2.3
 * @since 2.3
 */

public class RoutePlotQuestion extends Question implements RoutePlot {
	
	private Airport _airportD;
	private Airport _airportA;

	/**
	 * Creates a new route plotting Question bean.
	 * @param text the Question text
	 * @throws NullPointerException if text is null
	 */
	public RoutePlotQuestion(String text) {
		super(text);
	}

	/**
	 * Returns the arrival airport.
	 */
	public Airport getAirportA() {
		return _airportA;
	}

	/**
	 * Returns the departure airport.
	 */
	public Airport getAirportD() {
		return _airportD;
	}

	/**
	 * Returns the midpoint between the airports.
	 */
	public GeoLocation getMidPoint() {
		return new GeoPosition(_airportD).midPoint(_airportA);
	}
	
	/**
	 * Returns the distance between the airports.
	 */
	public int getDistance() {
		return new GeoPosition(_airportD).distanceTo(_airportA);
	}

	/**
	 * Updates the arrival airport.
	 */
	public void setAirportA(Airport a) {
		_airportA = a;
	}

	/**
	 * Updates the departure airport.
	 */
	public void setAirportD(Airport a) {
		_airportD = a;
	}
}