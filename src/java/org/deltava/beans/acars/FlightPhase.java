// Copyright 2008, 2011, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An enumeration to list valid ACARS flight phases. 
 * @author Luke
 * @version 7.3
 * @since 2.1
 */

public enum FlightPhase {
	
	UNKNOWN("Unknown"), PREFLIGHT("Pre-Flight"), PUSHBACK("Pushback"), TAXIOUT("Taxi Out"), TAKEOFF("Takeoff"), AIRBORNE("Airborne"), ROLLOUT("Rollout"), 
	TAXIIN("Taxi In"), ATGATE("At Gate"), SHUTDOWN("Shutdown"), COMPLETE("Completed"), ABORTED("Abort"), ERROR("Error"), PIREPFILE("File PIREP");
	
	private final String _name;

	FlightPhase(String name) {
		_name = name;
	}
	
	/**
	 * Returns the phase name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Exception-safe phase parser.
	 * @param phase the phase name
	 * @return a FlightPhase, or UNKNOWN
	 */
	public static FlightPhase fromString(String phase) {
		try {
			return FlightPhase.valueOf(phase);
		} catch (Exception e) {
			return UNKNOWN;
		}
	}
}