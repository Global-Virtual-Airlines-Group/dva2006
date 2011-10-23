// Copyright 2008, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An enumeration to list valid ACARS flight phases. 
 * @author Luke
 * @version 4.1
 * @since 2.1
 */

public enum FlightPhase {
	
	UNKNOWN("Unknown"), PREFLIGHT("Pre-Flight"), PUSHBACK("Pushback"), TAXIOUT("Taxi Out"), TAKEOFF("Takeoff"),
	AIRBORNE("Airborne"), ROLLOUT("Rollout"), TAXIIN("Taxi In"), ATGATE("At Gate"), SHUTDOWN("Shutdown"),
	COMPLETE("Completed"), ABORTED("Abort"), ERROR("Error"), PIREPFILE("File PIREP");
	
	private String _name;

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
}