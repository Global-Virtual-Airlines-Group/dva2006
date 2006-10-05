// Copyright 2004, 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.Date;
import java.util.Calendar;

import java.text.SimpleDateFormat;

/**
 * A class for storing Date/Time objects with Time Zone information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class DateTime implements java.io.Serializable, Comparable {
	
	private Calendar _dt;
	private TZInfo _tz = TZInfo.local();
	private boolean _showZone = true;
	
	private final SimpleDateFormat _df = new SimpleDateFormat("EEEE MMMM dd yyyy HH:mm:ss");

	/**
	 * Creates a DateTime given a Java date/time, defaulting to the JVM's time zone
	 * @param dt a Java date/time
	 * @see System#currentTimeMillis()
	 */
	public DateTime(Date dt) {
		this(dt, TZInfo.local());
	}

	/**
	 * Create a DateTime given a Java Date and a Time Zone wrapper. 
	 * @param dt The Date/Time component in local time
	 * @param tz the Time Zone
	 */
	public DateTime(Date dt, TZInfo tz) {
		super();
		_tz = tz;
		_dt = Calendar.getInstance(_tz.getTimeZone());
		_dt.setTime(dt);
		_dt.setTimeZone(_tz.getTimeZone());
	}
	
	/**
	 * Helper method to convert a date from one time zone to another.
	 * @param dt the date/time 
	 * @param tz the new time zone
	 * @return the new date/time
	 */
	public static Date convert(Date dt, TZInfo tz) {
		if (dt == null)
			return null;
		
		DateTime d = new DateTime(dt);
		d.convertTo(tz);
		return d.getDate();
	}
	
	/**
	 * Returns the date/time's local time value.
	 * @return the local date/time
	 * @see DateTime#getCalendar()
	 */
	public Date getDate() {
	   return _dt.getTime();
	}
	
	/**
	 * Returns the underlying Calendar object.
	 * @return the Calendar object with the local date/time
	 * @see DateTime#getDate()
	 */
	public Calendar getCalendar() {
		return _dt;
	}
	
	/**
	 * Returns the date/time converted to UTC
	 * @return the date/time converted to UTC
	 */
	public Date getUTC() {
		return new Date(_tz.getUTC(_dt.getTimeInMillis()));
	}
	
	/**
	 * Returns the Time Zone component
	 * @return the Time Zone wrapper for this Date/Time
	 */
	public TZInfo getTimeZone() {
		return _tz;
	}
	
	/**
	 * Compares to another Date/Time object by comparing the UTC times
	 * @see Comparable#compareTo(java.lang.Object)
	 * @throws ClassCastException
	 */
	public int compareTo(Object o2) {
		DateTime dt2 = (DateTime) o2;
		return (o2 == null) ? 1 : getUTC().compareTo(dt2.getUTC());
	}

	/**
	 * Calculates equality by comparing UTC times. Different local date/times that map to the same UTC time
	 * will be considered equal by this method.
	 */
	public boolean equals(Object o2) {
		return (o2 instanceof DateTime) ? (compareTo(o2) == 0) : false;
	}
	
	/**
	 * Updates the date/time format pattern string.
	 * @param pattern the pattern string
	 * @see SimpleDateFormat#applyPattern(String)
	 */
	public void setDateFormat(String pattern) {
	    _df.applyPattern(pattern);
	}
	
	/**
	 * Sets wether to display the time zone abbreviation or not.
	 * @param showZone TRUE if the time zone should be displayed, otherwise FALSE
	 */
	public void showZone(boolean showZone) {
	    _showZone = showZone;
	}
	
	/**
	 * Convert this Date/Time to a String. This formats the date using the DateFormat pattern
	 * "EEEE MMMM dd yyyy HH:mm:ss", and then appends the user-supplied time zone abbreviation
	 * if one is present.
	 * @see java.text.DateFormat#format(java.util.Date)
	 * @see TZInfo#getAbbr()
	 */
	public String toString() {
		StringBuilder buf = new StringBuilder(_df.format(_dt.getTime()));
		if ((_showZone) && (_tz.getAbbr() != null)) {
			buf.append(' ');
			buf.append(_tz.getAbbr());
		}
		
		return buf.toString();
	}
	
	/**
	 * The difference between two DateTimes, taking time zone differences into account
	 * @param dt2 The DateTime to compare to 
	 * @return The difference <b>in seconds</b> between the two DateTimes. This can be negative
	 * if the first DateTime is before the second. 
	 * @throws NullPointerException if the second date/time is null
	 */
	public long difference(DateTime dt2) {
		return (getUTC().getTime() - dt2.getUTC().getTime()) / 1000;
	}
	
	/**
	 * Updates the Time Zone component and converts the local time to what it is in the new Time Zone.
	 * @param tz2 the new Time Zone wrapper
	 * @throws NullPointerException if the Time Zone is null
	 */
	public void convertTo(TZInfo tz2) {
		if (tz2 == null)
			return;
		
		// Convert to UTC and move into the new zone
		long utc = _tz.getUTC(_dt.getTimeInMillis());
		_dt.setTimeInMillis(utc + tz2.getTimeZone().getOffset(utc));
		_dt.setTimeZone(tz2.getTimeZone());
		_tz = tz2;
	}
}