// Copyright 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * An enumeration to store lat/long conversion factors for Hemispheres on the Earth.
 * @author Luke
 * @version 7.2
 * @since 2.6
 */

public enum Hemisphere {
	N(1, -1), E(1, 1), W(-1, -1), S(-1, 1);
	
	private final int _latFactor;
	private final int _lngFactor;

	Hemisphere(int lat, int lng) {
		_latFactor = lat;
		_lngFactor = lng;
	}

	/**
	 * Returns the latitude conversion factor.
	 * @return the conversion factor
	 */
	public int getLatitudeFactor() {
		return _latFactor;
	}

	/**
	 * Returns the longitude conversion factror.
	 * @return the conversion factor
	 */
	public int getLongitudeFactor() {
		return _lngFactor;
	}
}