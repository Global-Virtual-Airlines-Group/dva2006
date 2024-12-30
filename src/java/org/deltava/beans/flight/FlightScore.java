// Copyright 2012, 2017, 2020, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import static java.awt.Color.*;

import org.deltava.beans.*;

/**
 * An enumeration of Flight Report scores. 
 * @author Luke
 * @version 11.4
 * @since 5.1
 */

public enum FlightScore implements EnumDescription, RGBColor {
	OPTIMAL(GREEN.darker().getRGB()), ACCEPTABLE(ORANGE.darker().getRGB()), DANGEROUS(RED.getRGB()), INCOMPLETE(GRAY.getRGB());
	
	private final int _color;

	FlightScore(int color) {
		_color = color & 0xFFFFFF; // strip alpha
	}
	
	@Override
	public int getColor() {
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