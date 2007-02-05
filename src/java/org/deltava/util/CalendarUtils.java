// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;

/**
 * A utility class to handle date/time functions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CalendarUtils {

	/**
	 * Adjust a date/time by a specified number of days.
	 * @param dt the date/time
	 * @param days the number of days to adjust
	 * @return the adjusted date/time
	 */
	public static Date adjust(Date dt, long days) {
		Calendar cld = getInstance(dt);
		cld.add(Calendar.DATE, (int) days);
		return cld.getTime();
	}

	/**
	 * Adjust a date/time by a specified number of milliseconds.
	 * @param dt the date/time
	 * @param ms the number of milliseconds to adjust
	 * @return the adjusted date/time
	 */
	public static Date adjustMS(Date dt, long ms) {
		Calendar cld = getInstance(dt);
		cld.add(Calendar.SECOND, (int) (ms / 1000));
		cld.add(Calendar.MILLISECOND, (int) (ms % 1000) * ((ms < 0) ? -1 : 1));
		return cld.getTime();
	}
	
	/**
	 * Returns a Calendar object initialized to a particular date/time.
	 * @param dt the date/time, or null if the current date/time
	 * @param clearTime TRUE if the time portion should be set to midnight, otherwise FALSE
	 * @return a Calendar object
	 */
	public static Calendar getInstance(Date dt, boolean clearTime) {
		return getInstance(dt, clearTime, 0);
	}
	
	/**
	 * Returns a Calendar object initialized to a particular date/time.
	 * @param dt the date/time, or null if the current date/time
	 * @param clearTime TRUE if the time portion should be set to midnight, otherwise FALSE
	 * @param days the number of days to adjust, or zero
	 * @return a Calendar object
	 */
	public static Calendar getInstance(Date dt, boolean clearTime, int days) {
		Calendar cld = Calendar.getInstance();
		if (dt != null)
			cld.setTime(dt);
		
		// Clear the time if requested
		if (clearTime) {
			cld.set(Calendar.HOUR_OF_DAY, 0);
			cld.set(Calendar.MINUTE, 0);
			cld.set(Calendar.SECOND, 0);
			cld.set(Calendar.MILLISECOND, 0);
		}
		
		// Adjust days if required
		if (days != 0)
			cld.add(Calendar.DATE, days);
		
		return cld;
	}

	/**
	 * Returns a Calendar object initialized to a particular date/time.
	 * @param dt the date/time, or null if the current date/time
	 * @return a Calendar object
	 * @see CalendarUtils#getInstance(Date, boolean, int)
	 */
	public static Calendar getInstance(Date dt) {
		return getInstance(dt, false, 0);
	}
}