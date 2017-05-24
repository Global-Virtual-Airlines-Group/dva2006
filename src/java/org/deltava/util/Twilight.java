// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

/**
 * An enumeration for storing twilight types. 
 * @author Luke
 * @version 7.4
 * @since 7.3
 */

public enum Twilight {
	EXACT(90), OFFICIAL(90.8333), CIVIL(96), NAUTICAL(102), ASTRO(108);

	private final double _degrees;
	
	Twilight(double degrees) {
		_degrees = degrees;
	}

	/**
	 * The distance the Sun is below the vertical.
	 * @return the distance in degrees
	 */
	public double getDegrees() {
		return _degrees;
	}
}