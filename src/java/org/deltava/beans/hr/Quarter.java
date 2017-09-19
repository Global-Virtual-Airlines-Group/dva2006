// Copyright 2010, 2011, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

import java.time.*;
import java.time.temporal.ChronoField;

/**
 * A bean to convert Dates into Quarters.
 * @author Luke
 * @version 8.0
 * @since 3.3
 */

public class Quarter implements java.io.Serializable, Comparable<Quarter> {

	private final int _year;
	private final int _qtr;
	
	/**
	 * Creates a Quarter from the current Date.
	 */
	public Quarter() {
		this(Instant.now());
	}
	
	/**
	 * Creates a new Quarter from a Date.
	 * @param dt the Date
	 * @throws NullPointerException if dt is null
	 */
	public Quarter(Instant dt) {
		super();
		ZonedDateTime zdt = ZonedDateTime.ofInstant(dt, ZoneOffset.UTC);
		_year = zdt.get(ChronoField.YEAR);
		_qtr = ((zdt.get(ChronoField.MONTH_OF_YEAR) - 1) / 3) + 1;
	}
	
	/**
	 * Creates a new Quarter from a year/quarter combination
	 * @param yq the year quarter
	 * @see Quarter#getYearQuarter()
	 */
	public Quarter(int yq) {
		super();
		_year = (yq / 10);
		_qtr = Math.max(1, (yq % 10));
	}
	
	/**
	 * Returns the year.
	 * @return the year
	 */
	public int getYear() {
		return _year;
	}
	
	/**
	 * Returns the quarter number.
	 * @return the quarter number
	 */
	public int getQuarter() {
		return _qtr;
	}
	
	/**
	 * Returns the year and quarter in the form of a five digit number as YYYYQ.
	 * @return the year and quarter
	 */
	public int getYearQuarter() {
		return (_year * 10) + _qtr;
	}
	
	/**
	 * Returns whether a date is in this Quarter.
	 * @param dt the date
	 * @return TRUE if in the Quarter, otherwise FALSE
	 */
	public boolean contains(Instant dt) {
		return equals(new Quarter(dt));
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("Q");
		buf.append(_qtr);
		buf.append(' ');
		buf.append(_year);
		return buf.toString();
	}
	
	@Override
	public int hashCode() { 
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof Quarter) ? (compareTo((Quarter) o) == 0) : false;
	}
	
	@Override
	public int compareTo(Quarter q2) {
		int tmpResult = Integer.compare(_year, q2._year);
		return (tmpResult == 0) ? Integer.compare(_qtr, q2._qtr) : tmpResult;
	}
}