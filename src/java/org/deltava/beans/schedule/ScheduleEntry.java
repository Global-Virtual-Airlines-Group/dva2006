// Copyright 2005 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.*;

/**
 * A class to store Schedule Entry information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ScheduleEntry extends Flight implements ViewEntry {
	
	private static final String[] SST = {"Concorde", "TU-144"};

	private DateTime _timeD;
	private DateTime _timeA;
	private int _length;
	private boolean _historic;
	private boolean _purge;

	/**
	 * Creates a new Schedule Entry object with a given flight.
	 * @param a the Airline
	 * @param fNumber the Flight Number
	 * @param leg the Leg Number
	 * @throws NullPointerException if the Airline Code is null
	 * @throws IllegalArgumentException if the Flight Report is zero or negative
	 * @throws IllegalArgumentException if the Leg is less than 1 or greater than 5
	 * @see Flight#setAirline(Airline)
	 * @see Flight#setFlightNumber(int)
	 * @see Flight#setLeg(int)
	 */
	public ScheduleEntry(Airline a, int fNumber, int leg) {
		super(a, fNumber, leg);
	}
	
	/**
	 * Returns the length of the flight, in hours <i>multiplied by 10</i>.
	 * @return the length of the flight
	 * @throws IllegalStateException if departure or arrival times are not set
	 * @see DateTime#difference(DateTime)
	 */
	public final int getLength() {
		if (_length > 0)
			return _length;
		else if ((_timeA == null) || (_timeD == null))
			throw new IllegalStateException("Arrival and Departure Times are not set");

		// Calculate flight time in seconds, and then divide by 3600 and multiply by 10
		long lengthS = _timeA.difference(_timeD);

		// If the length is negative (and we're not using an SST), then add 86400
		if ((lengthS < 0) && (StringUtils.arrayIndexOf(SST, getEquipmentType()) == -1))
			lengthS += 86400;

		return (int) (lengthS / 360);
	}

	/**
	 * Returns the departure time of the flight, with full timezone information. The date component of this value can be
	 * ignored.
	 * @return the full departure time of the flight
	 * @see ScheduleEntry#getDateTimeA()
	 */
	public DateTime getDateTimeD() {
		return _timeD;
	}

	/**
	 * Returns the arrival time of the flight, with full timezone information. The date component of this value can be
	 * ignored.
	 * @return the full arrival time of the flight
	 * @see ScheduleEntry#getDateTimeD()
	 */
	public DateTime getDateTimeA() {
		return _timeA;
	}

	/**
	 * Returns the departure time for this flght. <i>This time is in local time.</i> The date and timezone portions of
	 * this Date should be ignored.
	 * @return the departure time for this flight.
	 * @see ScheduleEntry#setTimeD(Date)
	 * @see ScheduleEntry#getTimeA()
	 * @see ScheduleEntry#getDateTimeD()
	 */
	public Date getTimeD() {
		return _timeD.getDate();
	}

	/**
	 * Returns the arrival time for this flght. <i>This time is in local time.</i> The date and timezone portions of
	 * this Date should be ignored.
	 * @return the arrival time for this flight.
	 * @see ScheduleEntry#setTimeA(Date)
	 * @see ScheduleEntry#getTimeD()
	 * @see ScheduleEntry#getDateTimeA()
	 */
	public Date getTimeA() {
		return _timeA.getDate();
	}

	/**
	 * Returns the database ID of the schedule entry. <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always
	 */
	public final int getID() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the "historic flight" flag value for this flight.
	 * @return TRUE if this is a historic flight, FALSE otherwise
	 * @see ScheduleEntry#setHistoric(boolean)
	 */
	public boolean getHistoric() {
		return _historic;
	}

	/**
	 * Returns if this flight can be purged from the schedule database before an automated import
	 * @return TRUE if the flight can be automatically purged from the database, otherwise FALSE
	 * @see ScheduleEntry#setPurge(boolean)
	 */
	public boolean getCanPurge() {
		return _purge;
	}

	/**
	 * Sets the database ID of this schedule entry. <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always
	 */
	public final void setID(int id) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the departure time for this flight.
	 * @param dt the departure time of the flight <i>in local time </i>. The date and time zone are ignored.
	 * @throws NullPointerException if the departure airport is not set
	 * @see ScheduleEntry#setTimeA(Date)
	 * @see ScheduleEntry#getTimeD()
	 * @see ScheduleEntry#getDateTimeD()
	 */
	public void setTimeD(Date dt) {
		TZInfo tz = (getAirportD() == null) ? TZInfo.local() : getAirportD().getTZ();
		_timeD = new DateTime(dt, tz);
	}

	/**
	 * Sets the arrival time for this flight.
	 * @param dt the arrival time of the flight <i>in local time </i>. The date and time zone are ignored.
	 * @throws NullPointerException if the arrival airport is not set
	 * @see ScheduleEntry#setTimeD(Date)
	 * @see ScheduleEntry#getTimeA()
	 * @see ScheduleEntry#getDateTimeA()
	 */
	public void setTimeA(Date dt) {
		TZInfo tz = (getAirportA() == null) ? TZInfo.local() : getAirportA().getTZ();
		if (dt.before(_timeD.getDate())) {
			Calendar cld = CalendarUtils.getInstance(dt);
			cld.add(Calendar.DATE, 1);
			_timeA = new DateTime(cld.getTime(), tz);
		} else {
			_timeA = new DateTime(dt, tz);
		}
	}

	/**
	 * Sets the length of a flight leg.
	 * @param len the length of a leg, <i>in hours mulitiplied by ten</i>.
	 * @throws IllegalArgumentException if len is negative
	 * @see ScheduleEntry#getLength()
	 */
	public void setLength(int len) {
		if (len < 0)
			throw new IllegalArgumentException("Invalid Flight Length - " + len);

		_length = len;
	}

	/**
	 * Updates this Schedule entry's "historic flight" flag.
	 * @param historic the new "historic flight" flag value
	 * @see ScheduleEntry#getHistoric()
	 * @see ScheduleEntry#getCanPurge()
	 */
	public void setHistoric(boolean historic) {
		_historic = historic;
	}

	/**
	 * Updates this Schedule entry's "no purge" flag. This typically is set on historic flights.
	 * @param purge the new "no purge" flag value
	 * @see ScheduleEntry#getCanPurge()
	 * @see ScheduleEntry#getHistoric()
	 */
	public void setPurge(boolean purge) {
		_purge = purge;
	}
	
	/**
	 * Returns the row CSS class name if displayed in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		return _historic ? "opt2" : null;
	}
}