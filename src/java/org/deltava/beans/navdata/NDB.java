// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * A bean to store Non-Directional Beacon information.
 * @author Luke
 * @version 2.2
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
	
	/**
	 * Returns the Google Earth palette code.
	 * @return 4
	 */
	public int getPaletteCode() {
		return 4;
	}
	
	/**
	 * Returns the Google Earth icon code.
	 * @return 57
	 */
	public int getIconCode() {
		return 57;
	}
}