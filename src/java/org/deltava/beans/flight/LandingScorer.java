// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

/**
 * A utility class to score FDR flight reports.
 * @author Luke
 * @version 10.4
 * @since 10.4
 */

public class LandingScorer {
	
	/**
	 * Optimal vertical speed at touchdown.
	 */
	public static final int OPT_VSPEED = -250;
	
	/**
	 * Vertical speed scoring factor.
	 */
	public static final double OPT_VSPEED_FACTOR = 0.6;
	
	/**
	 * Optimal distance from runway threshold at touchdown.
	 */
	public static final int OPT_DISTANCE = 1250;
	
	/**
	 * Runway distance scoring factor.
	 */
	public static final double OPT_DISTANCE_FACTOR = 0.4;
	
	/**
	 * Scores a landing based on delta from optimal vertical speed at touchdown and distance from runway threshold.
	 * @param vSpeed the vertical speed at touchdown in feet per minute
	 * @param rwyDistance the distance from the runway threshold in feet
	 * @return a flight score from 0 to 100, or -1 if it cannot be scored
	 */
	public static double score(int vSpeed, int rwyDistance) {
		if ((vSpeed > 0) || (Math.abs(rwyDistance) > 17500)) return - 1;
		int rawVS = Math.abs(OPT_VSPEED - vSpeed);
		int rawRD =Math.abs(OPT_DISTANCE - rwyDistance);
		double vs2 = Math.pow(rawVS, 1.275);
		double vs = Math.max(-0.5, 100 - (vs2/40)); 
		double rd2 = Math.pow(rawRD, 1.15);
		double rd = Math.max(-0.5, 100 - (rd2/50));
		return  Math.max(0, (vs * OPT_VSPEED_FACTOR) + (rd * OPT_DISTANCE_FACTOR));
	}
	
	/**
	 * Scores a landing based on delta from optimal vertical speed at touchdown and distance from runway threshold.
	 * @param vSpeed the vertical speed at touchdown in feet per minute
	 * @param rwyDistance the distance from the runway threshold in feet 
	 * @return a flight score
	 */
	public static double scoreLegacy(int vSpeed, int rwyDistance) {
		if ((vSpeed > 0) || (Math.abs(rwyDistance) > 17500)) return - 1;
		int rawVS = Math.abs(OPT_VSPEED - vSpeed);
		int rawRD =Math.abs(OPT_DISTANCE - rwyDistance);
		return (rawVS  * OPT_VSPEED_FACTOR) + (rawRD * OPT_DISTANCE_FACTOR);
	}
}