// Copyright 2011, 2017, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

/**
 * An enumeration for visibility distances.
 * @author Luke
 * @version 9.0
 * @since 4.1
 */

public enum Distance {
	FEET(1), METERS(3.2808399), SM(5280);

	private final double _feet;
	
	Distance(double feetPerUnit) {
		_feet = feetPerUnit;
	}

	/**
	 * Converts the specified value into feet.
	 * @param value the value
	 * @return the value converted into feet
	 */
	public int getFeet(double value) {
		return (int)Math.round(_feet * value);
	}
}