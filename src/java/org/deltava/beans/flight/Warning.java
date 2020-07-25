// Copyright 2017, 2018, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

/**
 * An enumeration to list flight condition warnings.
 * @author Luke
 * @version 9.1
 * @since 8.0
 */

public enum Warning {
	
	OVER250K("250 under 10K"), DESCENTRATE("Descent Rate"), BANK("Bank Angle"), PITCH("Pitch Angle"), GFORCE("G-Force"), TAXISPEED("Taxi Speed"), 
	NOFUEL("No Fuel", FlightScore.DANGEROUS), OVERSPEED("Overspeed", FlightScore.DANGEROUS), STALL("Stall", FlightScore.DANGEROUS), 
	AIRSPACE("Airspace", FlightScore.DANGEROUS), ALTITUDE("Altitude"), GEARSPEED("Gear Extension", FlightScore.DANGEROUS), 
	GEARUP("Gear Up"), CRASH("Crash", FlightScore.DANGEROUS), ENGOUT("Engine Out", FlightScore.DANGEROUS);

	private final String _desc;
	private final FlightScore _score;

	/**
	 * Creates the warning with an Acceptable score.
	 * @param description the warning text
	 */
	Warning(String description) {
		this(description, FlightScore.ACCEPTABLE);
	}
	
	/**
	 * Creates a warning.
	 * @param description the warning text
	 * @param score the FlightScore maximum
	 */
	Warning(String description, FlightScore score) {
		_desc = description;
		_score = score;
	}
	
	/**
	 * Returns a description of this warning condition.
	 * @return the Description
	 */
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
}