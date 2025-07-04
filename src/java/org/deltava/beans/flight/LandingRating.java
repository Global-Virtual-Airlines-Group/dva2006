// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

/**
 * An enumeration of landing score ratings.
 * @author Luke
 * @version 11.0
 * @since 10.4
 */

public enum LandingRating implements org.deltava.beans.EnumDescription {
	UNKNOWN(0), DANGEROUS(50), POOR(70), ACCEPTABLE(90), GOOD(101);
	
	private final int _maxScore;
	
	LandingRating(int maxScore) {
		_maxScore = maxScore;
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