// Copyright 2005, 2006, 2009, 2015, 2016, 2017, 2020, 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.time.*;
import java.time.temporal.ChronoUnit;

import org.deltava.beans.*;
import org.deltava.util.StringUtils;

/**
 * A class to store Schedule Entry information.
 * @author Luke
 * @version 10.2
 * @since 1.0
 */

public class ScheduleEntry extends Flight implements FlightTimes, ViewEntry {
	
	private static final long serialVersionUID = -4360178861586466272L;
	
	/**
	 * Variable equipment code.
	 */
	public static final String EQ_VARIES = "EQV";
	
	private ZonedDateTime _timeD;
	private ZonedDateTime _timeA;
	private int _length;
	
	private ScheduleSource _src;
	private String _codeShare;
	
	private boolean _historic;
	private boolean _academy;
	private boolean _hasDSTAdjustment;
	
	/**
	 * Adjusts the arrival date/time forward by a certain number of days.
	 */
	protected int _arrivalPlusDays;

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
	
	private static ZoneId getAirportTimeZone(Airport a) {
		return (a == null) ? ZoneOffset.UTC : a.getTZ().getZone();
	}
	
	/**
	 * Helper method to determine if flight is using a variable equipment type.
	 * @param f a Flight
	 * @return TRUE if equipment varies, otherwise FALSE
	 * @see ScheduleEntry#EQ_VARIES
	 */
	public static boolean isVariable(Flight f) {
		return EQ_VARIES.equalsIgnoreCase(f.getEquipmentType());
	}
	
	/**
	 * Returns the length of the flight, in hours <i>multiplied by 10</i>.
	 * @return the length of the flight
	 * @see ScheduleEntry#getDuration()
	 * @throws IllegalStateException if departure or arrival times are not set
	 */
	@Override
	public final int getLength() {
		if (_length > 0)
			return _length;
		
		// Calculate flight time in seconds, and then divide by 3600 and multiply by 10
		long lengthS = getDuration().getSeconds();
		return (int) (lengthS / 360);
	}

	@Override
	public final Duration getDuration() {
		if (!hasTimes())
			throw new IllegalStateException("Arrival and Departure Times are not set");
		
		Duration d = Duration.between(_timeD.toInstant(), _timeA.toInstant());
		return d.isNegative() ? d.negated() : d;
	}

	@Override
	public ZonedDateTime getTimeD() {
		return _timeD;
	}

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
	 * Returns if this flight is part of the Flight Academy.
	 * @return TRUE if the flight is part of the Flight Academy, otherwise FALSE
	 * @see ScheduleEntry#setAcademy(boolean)
	 */
	public boolean getAcademy() {
		return _academy;
	}
	
	/**
	 * Returns whether the arrival time has been adjusted for DST changes between the effective date of this Schedule
	 * Entry and the current date.
	 * @return TRUE if the arrival time has been adjusted, otherwise FALSE
	 * @see ScheduleEntry#adjustForDST(LocalDate)
	 */
	public boolean getHasDSTAdjustment() {
		return _hasDSTAdjustment;
	}
	
	/**
	 * If a codeshare, the flight code of the operator's flight.
	 * @return the flight code, or null if none
	 */
	public String getCodeShare() {
		return _codeShare;
	}
	
	/**
	 * Returns the original source of this Schedule Entry.
	 * @return the ScheduleSource, or null
	 */
	public ScheduleSource getSource() {
		return _src;
	}
	
	/**
	 * Returns the number of days forward to adjust the arrival time. 
	 * @return the number of days
	 */
	public int getArrivalPlusDays() {
		return _arrivalPlusDays;
	}

	/**
	 * Sets the database ID of this schedule entry. <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public final void setID(int id) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Adjusts the arrival time if the DST offsets for either Airport have changed from the effective date of this schedule etnry.
	 * @param dt the current date
	 * @return TRUE if the arrival time has been adjusted, otherwise FALSE
	 */
	public boolean adjustForDST(LocalDate dt) {
		if (!isPopulated() || !hasTimes()) return false;
		
		// Determine the proper departure/arrival date/times
		LocalDateTime ltD = LocalDateTime.of(dt, getTimeD().toLocalTime());
		LocalDateTime ltA = LocalDateTime.of(dt, getTimeA().toLocalTime());
		ZonedDateTime ztD = ltD.atZone(getAirportD().getTZ().getZone());
		ZonedDateTime ztA = ltA.atZone(getAirportA().getTZ().getZone());
		if (ztA.isBefore(ztD))
			ztA = ztA.plusDays(1);
		
		long od = getDuration().toSeconds(); long nd = Duration.between(ztD, ztA).toSeconds();
		if (od == nd) {
			_hasDSTAdjustment = false;
			return false;
		}
		
		_length = 0;
		_timeD = ztD;
		_timeA = ztA.plusSeconds(od - nd) ;
		_arrivalPlusDays = (int) ChronoUnit.DAYS.between(_timeD.toLocalDate(), _timeA.toLocalDate());
		_hasDSTAdjustment = true;
		return true;
	}
	
	/**
	 * Sets the departure time for this flight.
	 * @param dt the departure time of the flight
	 * @throws NullPointerException if the departure airport is not set
	 * @see ScheduleEntry#setTimeA(LocalDateTime)
	 * @see ScheduleEntry#getTimeD()
	 */
	public void setTimeD(LocalDateTime dt) {
		_length = 0; // reset length
		_timeD = ZonedDateTime.of(dt, getAirportTimeZone(getAirportD()));
	}

	/**
	 * Sets the arrival time for this flight.
	 * @param dt the arrival time of the flight in local time
	 * @throws NullPointerException if the arrival airport is not set
	 * @see ScheduleEntry#setTimeD(LocalDateTime)
	 * @see ScheduleEntry#getTimeA()
	 */
	public void setTimeA(LocalDateTime dt) {
		_length = 0; // reset length
		_timeA = ZonedDateTime.of(dt, getAirportTimeZone(getAirportA())); 
		if ((_timeD != null) && _timeA.isBefore(_timeD)) {
			_timeA = _timeA.plusDays(1);
			_arrivalPlusDays = 1;
		} else if (_timeD != null)
			_arrivalPlusDays = (int) ChronoUnit.DAYS.between(_timeD.toLocalDate(), dt.toLocalDate());
	}

	/**
	 * Sets the length of a flight leg.
	 * @param len the length of a leg, <i>in hours mulitiplied by ten</i>.
	 * @see ScheduleEntry#getLength()
	 */
	public void setLength(int len) {
		_length = Math.max(0, len);
	}

	/**
	 * Updates this Schedule entry's &quot;historic flight&quot; flag.
	 * @param historic the new &quot;historic flight&quot; flag value
	 * @see ScheduleEntry#getHistoric()
	 */
	public void setHistoric(boolean historic) {
		_historic = historic;
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
	 * If this is a codeshare flight, the flight code of the operator's flight.
	 * @param flightCode the flight code
	 */
	public void setCodeShare(String flightCode) {
		if (!StringUtils.isEmpty(flightCode))
			_codeShare = flightCode;
	}
	
	/**
	 * Updates the source of this entry.
	 * @param src the ScheduleSource
	 */
	public void setSource(ScheduleSource src) {
		_src = src;
	}
	
	@Override
	public String getRowClassName() {
		if (_academy)
			return "opt3";
		else if (_historic)
			return "opt2";
		
		return null;
	}
	
	@Override
	public String toString() {
		return getFlightCode();
	}
	
	@Override
	public int hashCode() {
		return getFlightCode().hashCode();
	}
}