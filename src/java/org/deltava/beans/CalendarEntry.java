// Copyright 2005, 2006, 2007, 2009 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to define beans that appear in a Calendar view table.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public interface CalendarEntry extends Comparable<Object> {

	/**
	 * Returns this entry's date for ordering in the Calendar.
	 * @return the date/time
	 */
	public java.util.Date getDate();
}