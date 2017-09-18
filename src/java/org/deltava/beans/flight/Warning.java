// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

/**
 * An enumeration to list flight condition warnings.
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public enum Warning {
	
	OVER250K("250 under 10K"), DESCENTRATE("Descent Rate"), BANK("Bank Angle"), PITCH("Pitch Angle"), GFORCE("G-Force"), TAXISPEED("Taxi Speed"), 
	NOFUEL("No Fuel", FlightScore.DANGEROUS), OVERSPEED("Overspeed", FlightScore.DANGEROUS), STALL("Stall", FlightScore.DANGEROUS), 
	AIRSPACE("Airpsace", FlightScore.DANGEROUS), ALTITUDE("Altitude"), GEARSPEED("Gear Extension", FlightScore.DANGEROUS), 
	GEARUP("Gear Up"), CRASH("Crash", FlightScore.DANGEROUS);

	private final String _desc;
	private final FlightScore _score;

	/**
	 * 
	 * @param description
	 */
	Warning(String description) {
		this(description, FlightScore.ACCEPTABLE);
	}
	
	/**
	 * 
	 * @param description
	 * @param score
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