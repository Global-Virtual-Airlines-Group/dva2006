// Copyright 2013, 2015, 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

/**
 * An enumeration to store GFS pressure levels.
 * @author Luke
 * @version 10.0
 * @since 5.2
 */

public enum PressureLevel {
	LOW(850, 5000), MIDLOW(650, 12000), MID(500, 18000), LOJET(300, 30000), JET(250, 34000), HIGH(200, 38600);
	
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