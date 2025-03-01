// Copyright 2017, 2018, 2020, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

/**
 * An enumeration to list flight condition warnings.
 * @author Luke
 * @version 11.5
 * @since 8.0
 */

public enum Warning implements org.deltava.beans.EnumDescription {
	
	OVER250K("250 under 10K", FlightScore.ACCEPTABLE, 3), DESCENTRATE("Descent Rate"), BANK("Bank Angle"), PITCH("Pitch Angle"), GFORCE("G-Force"),	TAXISPEED("Taxi Speed"), 
	NOFUEL("No Fuel", FlightScore.DANGEROUS), OVERSPEED("Overspeed", FlightScore.DANGEROUS, 2), STALL("Stall", FlightScore.DANGEROUS), AIRSPACE("Airspace", FlightScore.DANGEROUS),
	ALTITUDE("Altitude"), GEARSPEED("Gear Extension", FlightScore.DANGEROUS), GEARUP("Gear Up"), CRASH("Crash", FlightScore.DANGEROUS), ENGOUT("Engine Out", FlightScore.DANGEROUS);

	private final String _desc;
	private final FlightScore _score;
	private final int _minCount;

	/**
	 * Creates the warning with an Acceptable score.
	 * @param desc the warning text
	 */
	Warning(String desc) {
		this(desc, FlightScore.ACCEPTABLE, 1);
	}
	
	/**
	 * Creates a warning with a specific score.
	 * @param desc the warning text
	 * @param score the FlightScore maximum
	 */
	Warning(String desc, FlightScore score) {
		this(desc, score, 1);
	}
	
	/**
	 * Creates a warning wiht a specific score and invocation count.
	 * @param desc the warning text
	 * @param score the FlightScore maximum
	 * @param minCount the minimum instance count for this warning to adjust the flight score
	 */
	Warning(String desc, FlightScore score, int minCount) {
		_desc = desc;
		_score = score;
		_minCount = minCount;
	}
	
	@Override
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns the FlightScore to assign to a flight containing this warning condition.
	 * @return a FlightScore
	 */
	public FlightScore getScore() {
		return _score;
	}
	
	/**
	 * Returns the minimum instances of this warning to adjust the Flight Score.
	 * @return the minimum instance count
	 */
	public int getMinCount() {
		return _minCount;
	}
}