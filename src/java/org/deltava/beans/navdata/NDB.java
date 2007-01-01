// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * A bean to store Non-Directional Beacon information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NDB extends NavigationFrequencyBean {

	/**
	 * Creates a new NDB object.
	 * @param lat the latitude in degrees
	 * @param lon the longitude in degrees
	 */
	public NDB(double lat, double lon) {
		super(NDB, lat, lon);
	}

	/**
	 * Return the default Google Maps icon color.
	 * @return org.deltava.beans.MapEntry.ORANGE
	 */
	public String getIconColor() {
		return ORANGE;
	}
}