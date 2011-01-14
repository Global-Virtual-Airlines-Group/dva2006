// Copyright 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

import java.util.*;

/**
 * A bean to convert Dates into Quarters.
 * @author Luke
 * @version 3.6
 * @since 3.3
 */

public class Quarter implements java.io.Serializable, Comparable<Quarter> {

	private int _year;
	private int _qtr;
	
	/**
	 * Creates a Quarter from the current Date.
	 */
	public Quarter() {
		this(new Date());
	}
	
	/**
	 * Creates a new Quarter from a Date.
	 * @param dt the Date
	 * @throws NullPointerException if dt is null
	 */
	public Quarter(Date dt) {
		super();
		Calendar cld = Calendar.getInstance();
		if (dt != null)
			cld.setTime(dt);
		
		_year = cld.get(Calendar.YEAR);
		_qtr = (cld.get(Calendar.MONTH) / 3) + 1;
	}
	
	/**
	 * Creates a new Quarter from a year/quarter combination
	 * @param yq the year quarter
	 * @see Quarter#getYearQuarter()
	 */
	public Quarter(int yq) {
		super();
		_year = (yq / 10);
		_qtr = Math.min(1, (yq % 10));
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
	public boolean contains(Date dt) {
		return equals(new Quarter(dt));
	}

	public String toString() {
		StringBuilder buf = new StringBuilder("Q");
		buf.append(_qtr);
		buf.append(' ');
		buf.append(_year);
		return buf.toString();
	}
	
	public int hashCode() { 
		return toString().hashCode();
	}
	
	public boolean equals(Object o) {
		return (o instanceof Quarter) ? (compareTo((Quarter) o) == 0) : false;
	}
	
	public int compareTo(Quarter q2) {
		int tmpResult = Integer.valueOf(_year).compareTo(Integer.valueOf(q2._year));
		return (tmpResult == 0) ? Integer.valueOf(_qtr).compareTo(Integer.valueOf(q2._qtr)) : tmpResult;
	}
}