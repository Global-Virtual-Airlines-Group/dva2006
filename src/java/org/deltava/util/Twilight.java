// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.math.BigDecimal;

/**
 * An enumeration for storing twilight types. 
 * @author Luke
 * @version 7.3
 * @since 7.3
 */

public enum Twilight {
	OFFICIAL(90.8333), CIVIL(96), NAUTICAL(102), ASTRO(108);

	private final BigDecimal _degrees;
	
	Twilight(double degrees) {
		_degrees = BigDecimal.valueOf(degrees);
	}

	/**
	 * The distance the Sun is below the vertical.
	 * @return the distance in degrees
	 */
	public BigDecimal getDegrees() {
		return _degrees;
	}
}