// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.beans.Flight;

/**
 * A class to store Schedule Entry information with Dispatch route counts.
 * @author Luke
 * @version 2.3
 * @since 2.3
 */

public class ScheduleSearchEntry extends ScheduleEntry {
	
	private int _dspRoutes;

	/**
	 * Creates a new Schedule Entry object with a given flight.
	 * @param a the Airline
	 * @param number the Flight Number
	 * @param leg the Leg Number
	 * @throws NullPointerException if the Airline Code is null
	 * @throws IllegalArgumentException if the Flight Report is zero or negative
	 * @throws IllegalArgumentException if the Leg is less than 1 or greater than 5
	 * @see Flight#setAirline(Airline)
	 * @see Flight#setFlightNumber(int)
	 * @see Flight#setLeg(int)
	 */
	public ScheduleSearchEntry(Airline a, int number, int leg) {
		super(a, number, leg);
	}

	/**
	 * Returns the number of ACARS dispatch routes available between these airports.
	 * @return the number of routes
	 */
	public int getDispatchRoutes() {
		return _dspRoutes;
	}
	
	/**
	 * Updates the number of ACARS dispatch routes available between these airports.
	 * @param routes the number of routes
	 */
	public void setDispatchRoutes(int routes) {
		_dspRoutes = routes;
	}
	
	/**
	 * Returns the row CSS class name if displayed in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		String className = super.getRowClassName();
		if ((className == null) && (_dspRoutes > 0))
			return "opt1";
		
		return className;
	}
}