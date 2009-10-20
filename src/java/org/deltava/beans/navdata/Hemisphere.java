// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * An enumeration to store lat/long conversion factors for Hemispheres on the Earth.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public enum Hemisphere {
	
	N(1, -1), E(1, 1), W(-1, -1), S(-1, 1);
	
	private int _latFactor;
	private int _lngFactor;

	Hemisphere(int lat, int lng) {
		_latFactor = lat;
		_lngFactor = lng;
	}

	/**
	 * Returns the latitude conversion factor.
	 */
	public int getLatitudeFactor() {
		return _latFactor;
	}

	/**
	 * Retrurns the longitude conversion factror.
	 */
	public int getLongitudeFactor() {
		return _lngFactor;
	}
}