// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * A bean to store Flight Time schedule search results.
 * @author Luke
 * @version 4.2
 * @since 4.2
 */

public class FlightTime implements java.io.Serializable {

	private final int _time;
	private final boolean _hasHistoric;
	private final boolean _hasCurrent;
	
	/**
	 * Creates the bean.
	 * @param time the flight time in hours <i>multiplied by 10</i>
	 * @param hasHistoric TRUE if historic flights available, otherwise FALSE
	 * @param hasCurrent TRUE if current flights available, otherwise FALSE
	 */
	public FlightTime(int time, boolean hasHistoric, boolean hasCurrent) {
		super();
		_time = Math.max(0, time);
		_hasHistoric = hasHistoric;
		_hasCurrent = hasCurrent;
	}
	
	/**
	 * Returns the flight time.
	 * @return the flight time in hours multiplied by 10
	 */
	public int getFlightTime() {
		return _time;
	}
	
	/**
	 * Returns whether historic flights are available.
	 * @return TRUE if historic flights available, otherwise FALSE
	 */
	public boolean hasHistoric() {
		return _hasHistoric;
	}
	
	/**
	 * Returns whether current flights are available.
	 * @return TRUE if current flights available, otherwise FALSE
	 */
	public boolean hasCurrent() {
		return _hasCurrent;
	}
}