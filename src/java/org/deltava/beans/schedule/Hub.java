// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * A bean to track Airline Hub Airports. This is used to reduce the numbner of schedule API calls since most Airlines operate
 * in a Hub and Spoke pattern. We can therefore query arrivals and departures from the Hubs.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class Hub implements java.io.Serializable, Comparable<Hub> {

	private final Airline _a;
	private final Airport _ap;
	private int _destCount;
	
	/**
	 * Creates the bean. 
	 * @param a the Airline
	 * @param ap the Airport
	 */
	public Hub(Airline a, Airport ap) {
		super();
		_a = a;
		_ap = ap;
	}

	/**
	 * Returns the Airline.
	 * @return the Airline
	 */
	public Airline getAirline() {
		return _a;
	}
	
	/**
	 * Returns the Airport.
	 * @return the Airport
	 */
	public Airport getAirport() {
		return _ap;
	}
	
	/**
	 * Returns the number of destinations served by this Airline from this Airport.
	 * @return the number of destination Airports
	 */
	public int getDestinationCount() {
		return _destCount;
	}

	/**
	 * Updates the number of destinations served by this Airline from this Airport.
	 * @param cnt the number of destination Airports
	 */
	public void setDestinationCount(int cnt) {
		_destCount = cnt;
	}
	
	/**
	 * Helper method to check whether this Hub matches a particular Airline and Airport.  
	 * @param al the Airline
	 * @param a the Airport
	 * @return TRUE if the Airline and Airport are equal to this Hub, otherwise FALSE
	 */
	public boolean matches(Airline al, Airport a) {
		return _a.equals(al) && _ap.equals(a);
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return String.format("%s-%s", _a.getCode(), _ap.getIATA());
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof Hub h2) && _a.equals(h2._a) && _ap.equals(h2._ap);
	}
	
	@Override
	public int compareTo(Hub h2) {
		int tmpResult = _a.compareTo(h2._a);
		return (tmpResult == 0) ? _ap.compareTo(h2._ap) : tmpResult; 
	}
}