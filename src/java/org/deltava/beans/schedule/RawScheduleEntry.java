// Copyright 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;
import java.time.*;

import org.deltava.beans.Flight;

import org.deltava.util.StringUtils;

/**
 * A Schedule Entry with code share and day of week data. 
 * @author Luke
 * @version 9.0
 * @since 8.0
 */

public class RawScheduleEntry extends ScheduleEntry {

	private ScheduleSource _src;
	private int _line;
	
	private LocalDate _startDate;
	private LocalDate _endDate;
	
	private String _codeShare;
	private final Collection<DayOfWeek> _days = new TreeSet<DayOfWeek>();
	
	/**
	 * Creates the bean.
	 * @param a the Airline
	 * @param fNumber the flight number
	 * @param leg the leg number
	 */
	public RawScheduleEntry(Airline a, int fNumber, int leg) {
		super(a, fNumber, leg);
	}
	
	/**
	 * Creates the bean from an existing Flight.
	 * @param f the Flight
	 */
	public RawScheduleEntry(Flight f) {
		super(f.getAirline(), f.getFlightNumber(), f.getLeg());
	}

	/**
	 * If a codeshare, the flight code of the operator's flight.
	 * @return the flight code, or null if none
	 */
	public String getCodeShare() {
		return _codeShare;
	}
	
	/**
	 * Returns the day of the week this flight is operated on.
	 * @return a DayOfWeek enum
	 */
	public Collection<DayOfWeek> getDays() {
		return _days;
	}
	
	/**
	 * Returns a bitmap of the days operated.
	 * @return a bitmap of ordinal values
	 */
	public int getDayMap() {
		int bitmap = 0;
		for (DayOfWeek d : _days)
			bitmap |= (1 << d.ordinal());
		
		return bitmap; 
	}
	
	/**
	 * Returns the line number of this entry in the original source.
	 * @return the line number
	 */
	public int getLineNumber() {
		return _line;
	}
	
	/**
	 * Returns the original source.
	 * @return the ScheduleSource
	 */
	public ScheduleSource getSource() {
		return _src;
	}
	
	/**
	 * Returns the start of the operating range.
	 * @return the range start date
	 */
	public LocalDate getStartDate() {
		return _startDate;
	}
	
	/**
	 * Returns the end of the operating range.
	 * @return the range end date
	 */
	public LocalDate getEndDate() {
		return _endDate;
	}
	
	/**
	 * Returns if a flight is operated on a particular date.
	 * @param ld the operating date
	 * @return TRUE if the flight is operated on this date, otherwise FALSE
	 */
	public boolean operatesOn(LocalDate ld) {
		return _days.contains(ld.getDayOfWeek()) && !ld.isBefore(_startDate) && !ld.isAfter(_endDate);
	}
	
	public void addDayOfWeek(DayOfWeek d) {
		_days.add(d);
	}

	/**
	 * Sets the day of the week that this flight is operated on.
	 * @param bitmap a DayOfWeek bitmap
	 */
	public void setDayMap(int bitmap) {
		for (DayOfWeek d : DayOfWeek.values()) {
			if ((bitmap & (1 << d.ordinal())) != 0)
				_days.add(d);		
		}
	}
	
	/**
	 * Updates the start of the operating range.
	 * @param d the range start date
	 */
	public void setStartDate(LocalDate d) {
		_startDate = d;
	}
	
	/**
	 * Updates the end of the operating range.
	 * @param d the range end date
	 */
	public void setEndDate(LocalDate d) {
		_endDate = d;
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
	
	/**
	 * Updates the source file line number for this entry.
	 * @param ln the line number
	 */
	public void setLineNumber(int ln) {
		_line = ln;
	}
	
	/**
	 * Converts a RawScheduleEntry to a ScheduleEntry for the current date. 
	 * @param dt today as a LocalDate
	 * @return a SceduleEntry, or null if the flight does not operate
	 */
	public ScheduleEntry toToday(LocalDate dt) {
		if (!operatesOn(dt)) return null;
		
		ScheduleEntry se = new ScheduleEntry(getAirline(), getFlightNumber(), getLeg());
		se.setAirportD(getAirportD());
		se.setAirportA(getAirportA());
		se.setEquipmentType(getEquipmentType());
		se.setTimeD(LocalDateTime.of(dt, getTimeD().toLocalTime()));
		se.setTimeA(LocalDateTime.of(dt, getTimeA().toLocalTime()));
		se.setAcademy(getAcademy());
		se.setCanPurge(getCanPurge());
		se.setHistoric(getHistoric());
		return se;
	}
}