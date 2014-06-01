// Copyright 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A bean to store a location with a predetermined error value. 
 * @author Luke
 * @version 5.4
 * @since 5.4
 */

public class FuzzyPosition extends GeoPosition {

	private final float _h;
	
	/**
	 * Creates the bean.
	 * @param loc the GeoLocation
	 * @param h the error value 
	 */
	public FuzzyPosition(GeoLocation loc, float h) {
		super(loc);
		_h = h;
	}

	/**
	 * Returns the error value for this location.
	 * @return the error value in miles
	 */
	public float getH() {
		return _h;
	}
}