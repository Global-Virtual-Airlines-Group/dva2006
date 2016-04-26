// Copyright 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to describe beans with a start and an end time. 
 * @author Luke
 * @version 7.0
 * @since 3.1
 */

public interface TimeSpan extends CalendarEntry {

	/**
	 * The start date/time of this span.
	 * @return the start date/time
	 */
	public java.time.Instant getStartTime();
	
	/**
	 * The end date/time of this span.
	 * @return the end date/time
	 */
	public java.time.Instant getEndTime();
}