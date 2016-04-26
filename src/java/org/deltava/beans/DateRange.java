// Copyright 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.time.*;
import java.time.temporal.*;

import org.deltava.util.*;

/**
 * A bean to store date/time ranges.
 * @author Luke
 * @version 7.0
 * @since 3.6
 */

public class DateRange implements java.io.Serializable, Comparable<DateRange>, ComboAlias {

	private final String _label;
	private final Instant _startDate;
	private final Instant _endDate;

	/**
	 * Creates a date range for a specific day.
	 * @param dt a date/time within that day
	 * @return a DateRange
	 */
	public static DateRange createDay(Instant dt) {
		Instant sd = dt.truncatedTo(ChronoUnit.DAYS);
		Instant ed = sd.plus(1, ChronoUnit.DAYS);
		return new DateRange(sd, ed, StringUtils.format(sd, "MMM dd yyyy"));
	}
	
	/**
	 * Creates a date range for a specific week.
	 * @param dt a date/time within that day
	 * @return a DateRange
	 */
	public static DateRange createWeek(Instant dt) {
		Instant sd = dt.truncatedTo(ChronoUnit.DAYS);
		Instant ed = sd.plus(7, ChronoUnit.DAYS);
		return new DateRange(sd, ed, StringUtils.format(sd, "MMM dd yyyy"));
	}
	
	/**
	 * Creates a date range for a specific month.
	 * @param dt a date/time within that month
	 * @return a DateRange
	 */
	public static DateRange createMonth(Instant dt) {
		ZonedDateTime zdt = ZonedDateTime.ofInstant(dt, ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS);
		ZonedDateTime sd = zdt.minusDays(zdt.get(ChronoField.DAY_OF_MONTH) - 1);
		ZonedDateTime ed = sd.plus(1, ChronoUnit.MONTHS);
		return new DateRange(sd.toInstant(), ed.toInstant(), StringUtils.format(sd, "MMMM yyyy"));
	}
	
	/**
	 * Creates a date range for a specific year.
	 * @param dt a date/time within that year
	 * @return a DateRange
	 */
	public static DateRange createYear(Instant dt) {
		ZonedDateTime zdt = ZonedDateTime.ofInstant(dt, ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS);
		ZonedDateTime sd = zdt.minusDays(zdt.get(ChronoField.DAY_OF_YEAR) - 1);
		ZonedDateTime ed = sd.plus(1, ChronoUnit.YEARS);
		return new DateRange(sd.toInstant(), ed.toInstant(), StringUtils.format(sd, "MMMM yyyy"));
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
		if (dt == null)
			return false;
		
		return (!dt.isBefore(_startDate) && !dt.isAfter(_endDate));
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
		buf.append('-');
		buf.append(_endDate.toString());
		return buf.toString();
	}

	/**
	 * Compares two date ranges by comparing their start and end date/times.
	 */
	@Override
	public int compareTo(DateRange dr2) {
		int tmpResult = _startDate.compareTo(dr2._startDate);
		if (tmpResult == 0)
			tmpResult = _endDate.compareTo(dr2._endDate);
		
		return tmpResult;
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof DateRange) ? (compareTo((DateRange) o) == 0) : false;
	}
}