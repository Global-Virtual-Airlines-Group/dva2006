// Copyright 2005, 2006, 2009, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import org.deltava.beans.*;
import org.deltava.util.*;

/**
 * A class to store Schedule Entry information.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class ScheduleEntry extends Flight implements FlightTimes, ViewEntry {
	
	private static final String[] SST = {"Concorde", "TU-144"};
	
	private ZonedDateTime _timeD;
	private ZonedDateTime _timeA;
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
	 */
	@Override
	public final int getLength() {
		if (_length > 0)
			return _length;
		else if ((_timeA == null) || (_timeD == null))
			throw new IllegalStateException("Arrival and Departure Times are not set");

		// Calculate flight time in seconds, and then divide by 3600 and multiply by 10
		long lengthS = Duration.between(_timeD, _timeA).getSeconds();
		if (lengthS < 0)
			lengthS += 86400;

		return (int) (lengthS / 360);
	}

	/**
	 * Returns the departure time for this flght. <i>This time is in local time.</i> The date and timezone portions of
	 * this Date should be ignored.
	 * @return the departure time for this flight.
	 * @see ScheduleEntry#setTimeD(LocalDateTime)
	 * @see ScheduleEntry#getTimeA()
	 */
	@Override
	public ZonedDateTime getTimeD() {
		return _timeD;
	}

	/**
	 * Returns the arrival time for this flght. <i>This time is in local time.</i> The date and timezone portions of
	 * this Date should be ignored.
	 * @return the arrival time for this flight.
	 * @see ScheduleEntry#setTimeA(LocalDateTime)
	 * @see ScheduleEntry#getTimeD()
	 */
	@Override
	public ZonedDateTime getTimeA() {
		return _timeA;
	}

	/**
	 * Returns the database ID of the schedule entry. <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always
	 */
	@Override
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
	@Override
	public final void setID(int id) {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * Ensures each time has a date component of the current date if the year is less than 2001.
	 */
	private static LocalDateTime updateDate(LocalDateTime dt) {
		if (dt.get(ChronoField.YEAR) < 2001) {
			LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
			return now.plusMinutes(dt.get(ChronoField.MINUTE_OF_DAY));
		}
		
		return dt;
	}
	
	/**
	 * Sets the departure time for this flight.
	 * @param dt the departure time of the flight <i>in local time </i>. The date and time zone are ignored.
	 * @throws NullPointerException if the departure airport is not set
	 * @see ScheduleEntry#setTimeA(LocalDateTime)
	 * @see ScheduleEntry#getTimeD()
	 */
	public void setTimeD(LocalDateTime dt) {
		ZoneId tz = (getAirportD() == null) ? ZoneId.systemDefault() : getAirportD().getTZ().getZone();
		_length = 0; // reset length
		_timeD = ZonedDateTime.of(dt, tz);
	}

	/**
	 * Sets the arrival time for this flight.
	 * @param dt the arrival time of the flight <i>in local time </i>. The date and time zone are ignored.
	 * @throws NullPointerException if the arrival airport is not set
	 * @see ScheduleEntry#setTimeD(LocalDateTime)
	 * @see ScheduleEntry#getTimeA()
	 */
	public void setTimeA(LocalDateTime dt) {
		ZoneId tz = (getAirportD() == null) ? ZoneId.systemDefault() : getAirportA().getTZ().getZone();
		dt = updateDate(dt);
		_length = 0; // reset length
		if ((_timeD != null) && (dt.isBefore(_timeD.toLocalDateTime())) && (StringUtils.arrayIndexOf(SST, getEquipmentType()) == -1)) {
			_timeA = ZonedDateTime.of(dt.plusDays(1), tz);
			
			// Check if the flight time is longer than 20 hours, if so go back a day
			long lengthS = Duration.between(_timeD, _timeA).getSeconds();
			if (lengthS > 72000)
				_timeA = ZonedDateTime.of(dt, tz);
		} else
			_timeA = ZonedDateTime.of(dt, tz);
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
	@Override
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
	@Override
	public int hashCode() {
		return getFlightCode().hashCode();
	}
}