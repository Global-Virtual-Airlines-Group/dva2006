// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

/**
 * An enumeration of landing score ratings.
 * @author Luke
 * @version 10.4
 * @since 10.4
 */

public enum LandingRating {
	UNKNOWN(0), DANGEROUS(60), POOR(75), ACCEPTABLE(95), GOOD(101);
	
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
			if (score <= lr._maxScore)
				return lr;
		}
		
		return UNKNOWN;
	}
}