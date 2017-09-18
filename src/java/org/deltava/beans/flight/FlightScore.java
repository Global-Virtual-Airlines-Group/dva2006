// Copyright 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

/**
 * An enumeration of Flight Report scores. 
 * @author Luke
 * @version 8.0
 * @since 5.1
 */

public enum FlightScore {
	OPTIMAL("green"), ACCEPTABLE("orange"), DANGEROUS("red"), INCOMPLETE("grey");
	
	private final String _name;
	private final String _color;
	
	FlightScore(String color) {
		_name = name().substring(0, 1) + name().substring(1).toLowerCase();
		_color = color;
	}
	
	/**
	 * Returns the score label.
	 * @return the label
	 */
	public String getName() {
		return _name;
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