// Copyright 2008, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.Date;

import org.deltava.beans.Flight;

/**
 * A class to store Schedule Entry information with Dispatch route counts.
 * @author Luke
 * @version 6.3
 * @since 2.3
 */

public class ScheduleSearchEntry extends ScheduleEntry {
	
	private int _dspRoutes;
	private int _flownCount;
	private Date _lastFlown;

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
	 * Returns the number of times the route has been flown.
	 * @return the number of flights
	 */
	public int getFlightCount() {
		return _flownCount;
	}
	
	/**
	 * Returns the last date the route was flown on.
	 * @return the date of the last flight 
	 */
	public Date getLastFlownOn() {
		return _lastFlown;
	}
	
	/**
	 * Updates the number of ACARS dispatch routes available between these airports.
	 * @param routes the number of routes
	 */
	public void setDispatchRoutes(int routes) {
		_dspRoutes = Math.max(0, routes);
	}
	
	/**
	 * Updates the number of times the route has been flown.
	 * @param cnt the number of flights
	 */
	public void setFlightCount(int cnt) {
		_flownCount = Math.max(0, cnt);
	}
	
	/**
	 * Updates the last date the route was flown on.
	 * @param dt the date of the last flight 
	 */
	public void setLastFlownOn(Date dt) {
		_lastFlown = dt;
	}
	
	/**
	 * Returns the row CSS class name if displayed in a view table.
	 * @return the CSS class name
	 */
	@Override
	public String getRowClassName() {
		String className = super.getRowClassName();
		if ((className == null) && (_dspRoutes > 0))
			return "opt1";
		
		return className;
	}
}