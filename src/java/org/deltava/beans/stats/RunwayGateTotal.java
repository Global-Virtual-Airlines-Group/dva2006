// Copyright 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.schedule.Airline;

/**
 * A bean to store Runway/Gate usage by Airline. 
 * @author Luke
 * @version 11.1
 * @since 10.3
 */

class RunwayGateTotal implements java.io.Serializable, Comparable<RunwayGateTotal> {

	private final String _name;
	private final Airline _a;
	private final int _total;

	/**
	 * Creates the bean.
	 * @param name the Runway/Gate name
	 * @param a the Airline
	 * @param total the total number of uses
	 */
	RunwayGateTotal(String name, Airline a, int total) {
		super();
		_name = name;
		_a = a;
		_total = total;
	}
	
	/**
	 * Returns the Runway/Gate name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the Airline.
	 * @return the Airline
	 */
	public Airline getAirline() {
		return _a;
	}
	
	/**
	 * Returns the total number of times this Gate was used for a flight.
	 * @return the number of flights
	 */
	public int getTotal() {
		return _total;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_name);
		if (_a != null)
			buf.append('-').append(_a.getCode());
			
		return buf.toString();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public int compareTo(RunwayGateTotal gt) {
		int tmpResult = Integer.compare(_total, gt._total);
		if ((tmpResult == 0) && (_a != null)) tmpResult = _a.compareTo(gt._a); 
		return (tmpResult == 0) ? _name.compareTo(gt._name) : tmpResult;
	}
}