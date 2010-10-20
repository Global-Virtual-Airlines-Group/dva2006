// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

import java.util.*;

/**
 * A bean to convert Dates into Quarters.
 * @author Luke
 * @version 3.3
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
		_qtr = ((cld.get(Calendar.MONTH) + 1) / 3) + 1;
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
	
	public int compareTo(Quarter q2) {
		int tmpResult = Integer.valueOf(_year).compareTo(Integer.valueOf(q2._year));
		return (tmpResult == 0) ? Integer.valueOf(_qtr).compareTo(Integer.valueOf(q2._qtr)) : tmpResult;
	}
}