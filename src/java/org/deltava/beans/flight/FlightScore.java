// Copyright 2012, 2017, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.deltava.beans.EnumDescription;

/**
 * An enumeration of Flight Report scores. 
 * @author Luke
 * @version 9.2
 * @since 5.1
 */

public enum FlightScore implements EnumDescription {
	OPTIMAL("green"), ACCEPTABLE("orange"), DANGEROUS("red"), INCOMPLETE("grey");
	
	private final String _color;

	FlightScore(String color) {
		_color = color;
	}
	
	/**
	 * Returns the score color.
	 * @return the color
	 */
	public String getColor() {
		return _color;
	}
	
	/**
	 * Returns the higher of two Flight Scores. 
	 * @param fs1 the first FlightScore
	 * @param fs2 the second FlightScore
	 * @return the larger FlightScore
	 */
	public static FlightScore max(FlightScore fs1, FlightScore fs2) {
		return values()[Math.max(fs1.ordinal(), fs2.ordinal())];
	}
}