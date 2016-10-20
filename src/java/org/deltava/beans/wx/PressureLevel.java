// Copyright 2013, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

/**
 * An enumeration to store GFS pressure levels.
 * @author Luke
 * @version 7.2
 * @since 5.2
 */

public enum PressureLevel {

	LOW(875, 4000), MIDLOW(625, 12000), MID(475, 20000), LOJET(325, 28000), JET(275, 32000), HIGH(225, 38000);
	
	private final int _mb;
	private final int _alt;
	
	PressureLevel(int mb, int alt) {
		_mb = mb;
		_alt = alt;
	}
	
	/**
	 * Returns the pressure level.
	 * @return the pressure in millibars
	 */
	public int getPressure() {
		return _mb;
	}
	
	/**
	 * Returns the typical altitude. 
	 * @return the altitude in feet MSL
	 */
	public int getAltitude() {
		return _alt;
	}

	/**
	 * Returns the closest pressure level for an altitude.
	 * @param alt the altitude in feet
	 * @return the closest PressureLevel
	 */
	public static PressureLevel getClosest(int alt) {
		PressureLevel l = LOW; int delta = Math.abs(alt - l.getAltitude());
		PressureLevel[] lv = values();
		for (int x = 1; x < lv.length; x++) {
			PressureLevel pl = lv[x];
			int d = Math.abs(alt - pl.getAltitude());
			if (d <= delta) {
				delta = d;
				l = pl;
			}
		}
		
		return l;
	}
}