// Copyright 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.schedule.Airline;

/**
 * A bean to store Gate usage by Airline. 
 * @author Luke
 * @version 11.0
 * @since 10.3
 */

public class GateTotal implements java.io.Serializable, Comparable<GateTotal> {

	private final String _gateName;
	private final Airline _a;
	private final int _total;

	/**
	 * Creates the bean.
	 * @param name the Gate name
	 * @param a the Airline
	 * @param total the total number of uses
	 */
	GateTotal(String name, Airline a, int total) {
		super();
		_gateName = name;
		_a = a;
		_total = total;
	}
	
	/**
	 * Returns the Gate name.
	 * @return the name
	 */
	public String getGateName() {
		return _gateName;
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
		return String.format("%s-%s", _gateName, _a.getCode());
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public int compareTo(GateTotal gt) {
		int tmpResult = Integer.compare(_total, gt._total);
		if (tmpResult == 0) tmpResult = _a.compareTo(gt._a); 
		return (tmpResult == 0) ? _gateName.compareTo(gt._gateName) : tmpResult;
	}
}