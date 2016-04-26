// Copyright 2005, 2006, 2007, 2009, 2016 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to define beans that appear in a Calendar view table.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public interface CalendarEntry extends Comparable<Object> {

	/**
	 * Returns this entry's date for ordering in the Calendar.
	 * @return the date/time
	 */
	public java.time.Instant getDate();
}