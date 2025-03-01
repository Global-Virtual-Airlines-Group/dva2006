// Copyright 2012, 2017, 2020, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import static java.awt.Color.*;

import org.deltava.beans.*;

/**
 * An enumeration of Flight Report scores. 
 * @author Luke
 * @version 11.5
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
}