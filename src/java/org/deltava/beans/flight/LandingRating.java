// Copyright 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

/**
 * An enumeration of landing score ratings.
 * @author Luke
 * @version 12.4
 * @since 10.4
 */

public enum LandingRating implements org.deltava.beans.EnumDescription {
	UNKNOWN(0, "plain"), DANGEROUS(50, "error"), POOR(70, "warn"), ACCEPTABLE(90, "pri"), GOOD(101, "ok");
	
	private final int _maxScore;
	private final String _className;
	
	LandingRating(int maxScore, String className) {
		_maxScore = maxScore;
		_className = className;
	}
	
	/**
	 * Returns the CSS class name for this rating's description.
	 * @return the CSS class name
	 */
	public String getClassName() {
		return _className;
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