// Copyright 2012, 2016, 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.schedule.*;

/**
 * A bean to store route frequency data.
 * @author Luke
 * @version 10.2
 * @since 4.1
 */

public class RouteStats extends AbstractRoute implements Comparable<RouteStats> {
	
	private int _acarsFlights;

	/**
	 * Creates the bean.
	 * @param aD the departure Airport
	 * @param aA the arrival Airport
	 * @param freq the number of flights
	 */
	public RouteStats(Airport aD, Airport aA, int freq) {
		super(aD, aA);
		_frequency = Math.max(0, freq);
	}

	/**
	 * Adds flights.
	 * @param flights the total number of flights
	 * @param acarsFlights the number of ACARS flights
	 */
	public void add(int flights, int acarsFlights) {
		_acarsFlights += Math.max(0, acarsFlights);
		_frequency += Math.max(acarsFlights, flights);
	}
	
	/**
	 * Returns the number of ACARS flight legs. 
	 * @return the number of ACARS legs
	 */
	public int getACARSFlights() {
		return _acarsFlights;
	}
	
	@Override
	public int compareTo(RouteStats rs2) {
		return Integer.compare(_frequency, rs2._frequency);
	}
}