// Copyright 2005 Globa Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to define three-dimensional GeoLocations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface GeospaceLocation extends GeoLocation {

	/**
	 * Returns the altitude of the location above MSL.
	 * @return the altitude in feet
	 */
	public int getAltitude();
}