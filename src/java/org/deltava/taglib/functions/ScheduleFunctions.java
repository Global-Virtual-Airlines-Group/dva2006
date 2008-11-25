// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import org.deltava.beans.schedule.*;

/**
 * A JSP Function Library for Schedule-related functions. 
 * @author Luke
 * @version 2.3
 * @since 2.3
 */

public class ScheduleFunctions {

	/**
	 * Returns whether the schedule entry is the result of a search.
	 * @param e the ScheduleEntry bean
	 * @return TRUE if a {@link ScheduleSearchEntry} bean, otherwise FALSE
	 */
	public static boolean isSearchEntry(ScheduleEntry e) {
		return (e instanceof ScheduleSearchEntry);
	}
}