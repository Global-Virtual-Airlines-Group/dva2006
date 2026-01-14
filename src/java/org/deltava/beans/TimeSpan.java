// Copyright 2010, 2016, 2021, 2025, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.time.*;

/**
 * An interface to describe beans with a start and an end time. 
 * @author Luke
 * @version 12.4
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
	 * Validates that the start/end dates are present and is chronological order.
	 * @throws IllegalArgumentException if the dates fail validation
	 */
	default void validateDates() {
		if (!hasTimes())
			throw new IllegalArgumentException("Dates not populated");
		
		if (!getStartTime().isBefore(getEndTime()))
			throw new IllegalArgumentException(String.format("Start Date %s before %s", getStartTime(), getEndTime()));
	}
	
	/**
	 * The duration of this span, or null if both times are not set.
	 * @return a Duration, or null
	 */
	public default Duration getDuration() {
		return hasTimes() ? Duration.between(getStartTime(), getEndTime()) : null;
	}
	
	/**
	 * Returns whether the start and end times are populated.
	 * @return TRUE if both are populated, otherwise FALSE
	 */
	public default boolean hasTimes() {
		return (getStartTime() != null) && (getEndTime() != null);
	}
}