// Copyright 2011, 2012, 2016, 2019, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.time.*;
import java.time.temporal.*;

import org.deltava.util.*;

/**
 * A bean to store date/time ranges.
 * @author Luke
 * @version 11.0
 * @since 3.6
 */

public class DateRange implements java.io.Serializable, Comparable<DateRange>, ComboAlias {

	private final String _label;
	private final Instant _startDate;
	private final Instant _endDate;

	/**
	 * Creates a date range for a specific week.
	 * @param zdt a date/time within that day
	 * @return a DateRange
	 */
	public static DateRange createWeek(ZonedDateTime zdt) {
		Instant ed = zdt.toInstant().plus(7, ChronoUnit.DAYS);
		return new DateRange(zdt.toInstant(), ed, StringUtils.format(zdt, "MMM dd yyyy"));
	}
	
	/**
	 * Creates a date range for a specific month.
	 * @param zdt a date/time within that month
	 * @return a DateRange
	 */
	public static DateRange createMonth(ZonedDateTime zdt) {
		ZonedDateTime zdt2 = zdt.truncatedTo(ChronoUnit.DAYS);
		ZonedDateTime sd = zdt2.minusDays(zdt2.get(ChronoField.DAY_OF_MONTH) - 1);
		ZonedDateTime ed = sd.plus(1, ChronoUnit.MONTHS);
		return new DateRange(sd.toInstant(), ed.toInstant(), StringUtils.format(zdt, "MMMM yyyy"));
	}
	
	/**
	 * Creates a date range for a specific year.
	 * @param zdt a date/time within that year
	 * @return a DateRange
	 */
	public static DateRange createYear(ZonedDateTime zdt) {
		ZonedDateTime zdt2 = zdt.truncatedTo(ChronoUnit.DAYS);
		ZonedDateTime sd = zdt2.minusDays(zdt2.get(ChronoField.DAY_OF_YEAR) - 1);
		ZonedDateTime ed = sd.plus(1, ChronoUnit.YEARS);
		return new DateRange(sd.toInstant(), ed.toInstant(), StringUtils.format(zdt, "yyyy"));
	}
	
	/**
	 * Parses a Date range combo alias.
	 * @param alias the alias
	 * @return a DateRange, or null if unparseable
	 * @see DateRange#getComboAlias()
	 */
	public static DateRange parse(String alias) {
		if (alias == null) return null;
		int pos = alias.indexOf('-');
		if (pos == -1)
			return null;
		
		try {
			long st = Long.parseLong(alias.substring(0, pos));
			long len = Long.parseLong(alias.substring(pos + 1));
			return new DateRange(Instant.ofEpochMilli(st), Instant.ofEpochMilli(st + len));
		} catch (NumberFormatException nfe) {
			return null;
		}
	}
	
	/**
	 * Creates a date range.
	 * @param startDate the start date/time
	 * @param endDate the end date/time
	 */
	public DateRange(Instant startDate, Instant endDate) {
		this(startDate, endDate, null);
	}
	
	/**
	 * Creates a date range.
	 * @param startDate the start date/time
	 * @param endDate the end date/time
	 * @param label the label override
	 */
	private DateRange(Instant startDate, Instant endDate, String label) {
		super();
		_startDate = startDate;
		_endDate = endDate;
		_label = (label == null) ? _startDate.toString() : label;
	}

	/**
	 * Returns the start of the range.
	 * @return the start date/time
	 */
	public Instant getStartDate() {
		return _startDate;
	}
	
	/**
	 * Returns the end of the range.
	 * @return the end date/time
	 */
	public Instant getEndDate() {
		return _endDate;
	}
	
	/**
	 * Returns the range label.
	 * @return the label
	 */
	public String getLabel() {
		return _label;
	}
	
	/**
	 * Returns whether a date/time is contained within this range. 
	 * @param dt the date/time
	 * @return TRUE if contained, otherwise FALSE
	 */
	public boolean contains(Instant dt) {
		return (dt == null) ? false : (!dt.isBefore(_startDate) && !dt.isAfter(_endDate));
	}
	
	/**
	 * Returns the size of the range.
	 * @return the size in milliseconds
	 */
	public long getLength() {
		return (_endDate.toEpochMilli() - _startDate.toEpochMilli());
	}
	
	@Override
	public String getComboName() {
		return _label;
	}
	
	@Override
	public String getComboAlias() {
		StringBuilder buf = new StringBuilder();
		buf.append(_startDate.toEpochMilli());
		buf.append('-');
		buf.append(getLength());
		return buf.toString();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_startDate.toString());
		buf.append('-').append(_endDate.toString());
		return buf.toString();
	}

	/**
	 * Compares two date ranges by comparing their start and end date/times.
	 */
	@Override
	public int compareTo(DateRange dr2) {
		int tmpResult = _startDate.compareTo(dr2._startDate);
		return (tmpResult == 0) ? _endDate.compareTo(dr2._endDate) : tmpResult;
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof DateRange dr) ? (compareTo(dr) == 0) : false;
	}
}