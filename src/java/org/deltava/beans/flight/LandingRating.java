// Copyright 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.deltava.beans.*;

/**
 * An enumeration of landing score ratings.
 * @author Luke
 * @version 12.4
 * @since 10.4
 */

public enum LandingRating implements EnumDescription, RGBColor {
	UNKNOWN(0, 0x0000), DANGEROUS(50, 0xE02010), POOR(70, 0xFF8040), ACCEPTABLE(90, 0x0000A1), GOOD(101, 0x008080);
	
	private final int _maxScore;
	private final int _rgb;
	
	LandingRating(int maxScore, int rgb) {
		_maxScore = maxScore;
		_rgb = rgb; 
	}
	
	@Override
	public int getColor() {
		return _rgb;
	}
	
	/**
	 * Converts a raw score into a rating.
	 * @param score the score
	 * @return a LandingRating, or UNKNOWN
	 */
	public static LandingRating rate(int score) {
		for (int x = 0; x < values().length; x++) {
			LandingRating lr = values()[x];
			if (score < lr._maxScore)
				return lr;
		}
		
		return UNKNOWN;
	}
}