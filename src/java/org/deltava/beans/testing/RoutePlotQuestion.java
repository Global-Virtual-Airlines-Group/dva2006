// Copyright 2008, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.*;

/**
 * A bean to store route plotting Examination questions.
 * @author Luke
 * @version 7.0
 * @since 2.3
 */

public class RoutePlotQuestion extends MultiChoiceQuestion implements RoutePlot {
	
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

	@Override
	public Airport getAirportA() {
		return _airportA;
	}

	@Override
	public Airport getAirportD() {
		return _airportD;
	}

	@Override
	public GeoLocation getMidPoint() {
		return new GeoPosition(_airportD).midPoint(_airportA);
	}

	@Override
	public int getDistance() {
		return new GeoPosition(_airportD).distanceTo(_airportA);
	}

	@Override
	public void setAirportA(Airport a) {
		_airportA = a;
	}

	@Override
	public void setAirportD(Airport a) {
		_airportD = a;
	}
}