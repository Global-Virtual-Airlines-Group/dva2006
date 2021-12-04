// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

/**
 * A bean to store Online Event statistics.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class EventStats {
	
	private final String _label;
	private int _cnt;
	private int _signups;
	private int _flights;
	private int _pilotSignups;
	private int _pilotFlights;

	/**
	 * Creates the bean.
	 * @param label the lasbel 
	 */
	public EventStats(String label) {
		super();
		_label = label;
	}

	/**
	 * Returns the statistics label.
	 * @return the label
	 */
	public String getLabel() {
		return _label;
	}
	
	/**
	 * Returns the number of Online Events.
	 * @return the number of Events
	 */
	public int getCount() {
		return _cnt;
	}
	
	/**
	 * Returns the number of signups for these Online Events.
	 * @return the number of signups
	 */
	public int getSignups() {
		return _signups;
	}

	/**
	 * Returns the number of approved Flight Reports for these Online Events.
	 * @return the number of Flights
	 */
	public int getFlights() {
		return _flights;
	}
	
	/**
	 * Returns the number of distinct Pilots who signed up for at least one Online Event.
	 * @return the number of distinct Pilots
	 */
	public int getPilotSignups() {
		return _pilotSignups;
	}
	
	/**
	 * Returns the number of distinct Pilots who filed an approved Flight Report for at least one Online Event.
	 * @return the number of distinct Pilots
	 */
	public int getPilotFlights() {
		return _pilotFlights;
	}

	/**
	 * Updates the number of Online Events within this period.
	 * @param cnt the number of Events
	 */
	public void setCount(int cnt) {
		_cnt = cnt;
	}

	/**
	 * Updates the number of signups for these Online Events.
	 * @param cnt the number of signups
	 */
	public void setSignups(int cnt) {
		_signups = cnt;
	}

	/**
	 * Updates the number of approved Flight Reports for these Online Events.
	 * @param cnt the number of Flight Reports
	 */
	public void setFlights(int cnt) {
		_flights = cnt;
	}

	/**
	 * Updates the number of distinct Pilots who signed up for at least one Online Event.
	 * @param cnt the number of distinct Pilots
	 */
	public void setPilotSignups(int cnt) {
		_pilotSignups = cnt;
	}

	/**
	 * Updates the number of distinct Pilots who filed an approved Flight Report for at least one Online Event.
	 * @param cnt the number of distinct Pilots
	 */
	public void setPilotFlights(int cnt) {
		_pilotFlights = cnt;
	}
}