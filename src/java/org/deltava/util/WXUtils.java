// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

/**
 * A utility class for weather calculations.
 * @author Luke
 * @version 11.5
 * @since 11.5
 */

public class WXUtils {
	
	// Static class
	private WXUtils() {
		super();
	}
	
	/**
	 * Converts pascals to inches of mercury.
	 * @param p the pressure in pascals
	 * @return the pressure in inches of mercury
	 */
	public static double toHG(int p) {
		return p * 0.0002953;
	}
	
	/**
	 * Calculates atmospheric pressure at a given altitude. This function is unit-agnostic.
	 * @param altDelta the altitude delta from current in feet
	 * @param temp the current temperature in degrees Kelvin
	 * @param p the current pressure
	 * @return the pressure at the specified altitude delta from current
	 */
	public static double getPressure(int altDelta, int temp, int p) {
		
		double h = altDelta * -0.3048; // convert to meters
		double e = StrictMath.exp((-9.81 * 0.02896 * h) / (8.31432 * temp));
		return p * e;
	}
}