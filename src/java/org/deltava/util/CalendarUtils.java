// Copyright 2005 Global Virtual Airlines Group. All Rights Reserved.
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
	 * Returns a Calendar object initialized to a particular date/time.
	 * @param dt the date/time, or null if the current date/time
	 * @return a Calendar object
	 */
	public static Calendar getInstance(Date dt) {
		Calendar cld = Calendar.getInstance();
		if (dt != null)
			cld.setTime(dt);
		
		return cld;
	}
}