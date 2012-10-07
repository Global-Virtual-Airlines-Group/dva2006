// Copyright 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;

import org.deltava.util.*;

/**
 * A bean to store date/time ranges.
 * @author Luke
 * @version 4.2
 * @since 3.6
 */

public class DateRange implements java.io.Serializable, Comparable<DateRange>, ComboAlias {

	private final String _label;
	private final Date _startDate;
	private final Date _endDate;

	/**
	 * Creates a date range for a specific day.
	 * @param dt a date/time within that day
	 * @return a DateRange
	 */
	public static DateRange createDay(Date dt) {
		Date sd = CalendarUtils.getInstance(dt, true).getTime();
		return new DateRange(sd, CalendarUtils.adjust(sd, 1), StringUtils.format(sd, "MMM dd yyyy"));
	}
	
	/**
	 * Creates a date range for a specific week.
	 * @param dt a date/time within that day
	 * @return a DateRange
	 */
	public static DateRange createWeek(Date dt) {
		Calendar sc = CalendarUtils.getInstance(dt, true);
		return new DateRange(sc.getTime(), CalendarUtils.adjust(sc.getTime(), 7), StringUtils.format(sc.getTime(), "MMM dd yyyy"));
	}
	
	/**
	 * Creates a date range for a specific month.
	 * @param dt a date/time within that month
	 * @return a DateRange
	 */
	public static DateRange createMonth(Date dt) {
		Calendar sc = CalendarUtils.getInstance(dt, true);
		Calendar ec = CalendarUtils.getInstance(sc.getTime());
		ec.add(Calendar.MONTH, 1);
		return new DateRange(sc.getTime(), ec.getTime(), StringUtils.format(sc.getTime(), "MMMM yyyy"));
	}
	
	/**
	 * Creates a date range for a specific year.
	 * @param dt a date/time within that year
	 * @return a DateRange
	 */
	public static DateRange createYear(Date dt) {
		Calendar sc = CalendarUtils.getInstance(dt, true);
		Calendar ec = CalendarUtils.getInstance(sc.getTime());
		ec.add(Calendar.YEAR, 1);
		return new DateRange(sc.getTime(), ec.getTime(), String.valueOf(sc.get(Calendar.YEAR)));
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
			return new DateRange(new Date(st), new Date(st + len));
		} catch (NumberFormatException nfe) {
			return null;
		}
	}
	
	/**
	 * Creates a date range.
	 * @param startDate the start date/time
	 * @param endDate the end date/time
	 */
	public DateRange(Date startDate, Date endDate) {
		this(startDate, endDate, null);
	}
	
	/**
	 * Creates a date range.
	 * @param startDate the start date/time
	 * @param endDate the end date/time
	 * @param label the label override
	 */
	private DateRange(Date startDate, Date endDate, String label) {
		super();
		_startDate = startDate;
		_endDate = endDate;
		_label = (label == null) ? _startDate.toString() : label;
	}

	/**
	 * Returns the start of the range.
	 * @return the start date/time
	 */
	public Date getStartDate() {
		return _startDate;
	}
	
	/**
	 * Returns the end of the range.
	 * @return the end date/time
	 */
	public Date getEndDate() {
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
	public boolean contains(Date dt) {
		if (dt == null)
			return false;
		
		return (dt.getTime() >= _startDate.getTime()) && (dt.getTime() <= _endDate.getTime()); 
	}
	
	/**
	 * Returns the size of the range.
	 * @return the size in milliseconds
	 */
	public long getLength() {
		return (_endDate.getTime() - _startDate.getTime());
	}
	
	public String getComboName() {
		return _label;
	}
	
	public String getComboAlias() {
		StringBuilder buf = new StringBuilder();
		buf.append(_startDate.getTime());
		buf.append('-');
		buf.append(getLength());
		return buf.toString();
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder(_startDate.toString());
		buf.append('-');
		buf.append(_endDate.toString());
		return buf.toString();
	}

	/**
	 * Compares two date ranges by comparing their start and end date/times.
	 */
	public int compareTo(DateRange dr2) {
		int tmpResult = _startDate.compareTo(dr2._startDate);
		if (tmpResult == 0)
			tmpResult = _endDate.compareTo(dr2._endDate);
		
		return tmpResult;
	}
	
	public boolean equals(Object o) {
		return (o instanceof DateRange) ? (compareTo((DateRange) o) == 0) : false;
	}
}