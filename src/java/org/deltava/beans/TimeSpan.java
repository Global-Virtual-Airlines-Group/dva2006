// Copyright 2010, 2016, 2021, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.time.*;

/**
 * An interface to describe beans with a start and an end time. 
 * @author Luke
 * @version 11.6
 * @since 3.1
 */

public interface TimeSpan extends CalendarEntry {

	/**
	 * The start date/time of this span.
	 * @return the start date/time
	 */
	public Instant getStartTime();
	
	/**
	 * The end date/time of this span.
	 * @return the end date/time
	 */
	public Instant getEndTime();
	
	/**
	 * The duration of this span, or null if both times are not set.
	 * @return a Duration, or null
	 */
	public default Duration getDuration() {
		return ((getStartTime() == null) || (getEndTime() == null)) ? null : Duration.between(getStartTime(), getEndTime());
	}
	
	/**
	 * Returns whether the start and end times are populated.
	 * @return TRUE if both are populated, otherwise FALSE
	 */
	public default boolean hasTimes() {
		return (getStartTime() != null) && (getEndTime() != null);
	}
}