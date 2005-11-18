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
	public static Date adjust(Date dt, int days) {
		Calendar cld = Calendar.getInstance();
		cld.setTime(dt);
		cld.add(Calendar.DATE, days);
		return cld.getTime();
	}
}