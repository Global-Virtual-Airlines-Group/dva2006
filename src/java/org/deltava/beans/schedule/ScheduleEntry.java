// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.util.*;

/**
 * A class to store Schedule Entry information.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class ScheduleEntry extends Flight implements ViewEntry {
	
	private static final String[] SST = {"Concorde", "TU-144"};
	
	private final Calendar today = CalendarUtils.getInstance(null, true);

	private DateTime _timeD;
	private DateTime _timeA;
	private int _length;
	private boolean _historic;
	private boolean _academy;
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
		if (lengthS < 0)
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
	 * Returns if this flight can be purged from the schedule database before an automated import.
	 * @return TRUE if the flight can be automatically purged from the database, otherwise FALSE
	 * @see ScheduleEntry#setCanPurge(boolean)
	 */
	public boolean getCanPurge() {
		return _purge;
	}
	
	/**
	 * Returns if this flight is part of the Flight Academy.
	 * @return TRUE if the flight is part of the Flight Academy, otherwise FALSE
	 * @see ScheduleEntry#setAcademy(boolean)
	 */
	public boolean getAcademy() {
		return _academy;
	}

	/**
	 * Sets the database ID of this schedule entry. <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always
	 */
	public final void setID(int id) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Ensures each time has a date component of the current date if the year is less than 2001.
	 */
	private Date updateDate(Date dt) {
		Calendar cld = CalendarUtils.getInstance(dt);
		if (cld.get(Calendar.YEAR) < 2001) {
			cld.set(Calendar.YEAR, today.get(Calendar.YEAR));
			cld.set(Calendar.MONTH, today.get(Calendar.MONTH));
			cld.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
		}
		
		return cld.getTime();
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
		_length = 0; // reset length
		_timeD = new DateTime(updateDate(dt), tz);
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
		dt = updateDate(dt);
		TZInfo tz = (getAirportA() == null) ? TZInfo.local() : getAirportA().getTZ();
		_length = 0; // reset length
		if ((_timeD != null) && (dt.before(_timeD.getDate())) && (StringUtils.arrayIndexOf(SST, getEquipmentType()) == -1)) {
			Calendar cld = CalendarUtils.getInstance(dt);
			cld.add(Calendar.DATE, 1);
			_timeA = new DateTime(cld.getTime(), tz);
			
			// Check if the flight time is longer than 24 hours, if so go back a day
			long lengthS = _timeA.difference(_timeD);
			if (lengthS > 86400)
				_timeA = new DateTime(dt, tz);
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
	 * Updates this Schedule entry's &quot;historic flight&quot; flag.
	 * @param historic the new &quot;historic flight&quot; flag value
	 * @see ScheduleEntry#getHistoric()
	 * @see ScheduleEntry#getCanPurge()
	 */
	public void setHistoric(boolean historic) {
		_historic = historic;
	}

	/**
	 * Updates this Schedule entry's &quot;no purge&quot; flag. This typically is set on historic flights.
	 * @param purge the new &quot;no purge&quot; flag value
	 * @see ScheduleEntry#getCanPurge()
	 * @see ScheduleEntry#getHistoric()
	 */
	public void setCanPurge(boolean purge) {
		_purge = purge;
	}
	
	/**
	 * Updates this Schedule entry's &quot;Flight Academy flight&quot; flag.
	 * @param academy TRUE if the Flight is part of the Academy, otherwise FALSE
	 * @see ScheduleEntry#getAcademy()
	 */
	public void setAcademy(boolean academy) {
		_academy = academy;
	}
	
	/**
	 * Returns the row CSS class name if displayed in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		if (_academy)
			return "opt3";
		else if (_historic)
			return "opt2";
		
		return null;
	}
	
	/**
	 * Returns the hash code of the flight code.
	 */
	public int hashCode() {
		return getFlightCode().hashCode();
	}
}