// Copyright 2011, 2012, 2018, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.deltava.beans.ComboAlias;

/**
 * An enumeration of ETOPS classifications. 
 * @author Luke
 * @version 10.0
 * @since 4.1
 */

public enum ETOPS implements ComboAlias {
	ETOPS60(60), ETOPS75(75), ETOPS90(90), ETOPS120(120), ETOPS138(138), ETOPS180(180), ETOPS207(207), ETOPS240(240), ETOPS330(330), INVALID(-1);
	
	// ETOPS rule speed - 389kts
	private static final double EO_SPEED = (389 * 1.15078); // convert kts to mph
	
	private final int _time;
	private final int _range;
	
	ETOPS(int time) {
		_time = time;
		_range = (int)Math.round(time * EO_SPEED / 60f);
	}

	/**
	 * Returns the single-engine operating time.
	 * @return the time in minutes
	 */
	public int getTime() {
		return _time;
	}
	
	/**
	 * Returns the maximum single-engine range. 
	 * @return the range in nautical miles
	 */
	public int getRange() {
		return _range; 
	}
	
	/**
	 * Returns the ETOPS classification to fly a particular distance. 
	 * @param range the range in nautical miles
	 * @return the ETOPS classification, or null if exceeding ETOPS207
	 */
	public static ETOPS getClassification(int range) {
		for (ETOPS e : values()) {
			if (e.getRange() > range)
				return e;
		}
		
		return INVALID;
	}
	
	/**
	 * Mechanism to calculate value from ordinal while using -1 for invalid.
	 * @param code the ETOPS ordinal, or -1 for invalid
	 * @return an ETOPS
	 */
	public static ETOPS fromCode(int code) {
		return (code < 0) ? INVALID : values()[code];
	}

	@Override
	public String getComboAlias() {
		return String.valueOf((this == INVALID) ? -1 : ordinal());
	}

	@Override
	public String getComboName() {
		return name();
	}
}