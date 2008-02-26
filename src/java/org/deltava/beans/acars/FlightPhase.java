// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An enumeration to list valid ACARS flight phases. 
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public enum FlightPhase {
	
	UNKNOWN(0),
	PREFLIGHT(1),
	PUSHBACK(2),
	TAXIOUT(3),
	TAKEOFF(4),
	AIRBORNE(5),
	ROLLOUT(6),
	TAXIIN(7),
	ATGATE(8),
	SHUTDOWN(9),
	COMPLETE(10),
	ABORTED(11),
	ERROR(12),
	PIREPFILE(13);

	private int _phase;
	
	FlightPhase(int phase) {
		_phase = phase;
	}
	
	/**
	 * Returns the flight phase.
	 * @return the phase code
	 */
	public int getPhase() {
		return _phase;
	}
}