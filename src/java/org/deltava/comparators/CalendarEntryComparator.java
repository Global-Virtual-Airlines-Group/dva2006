// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.comparators;

import java.util.Comparator;

import org.deltava.beans.CalendarEntry;

/**
 * A comparator to compare Calendar view entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CalendarEntryComparator implements Comparator<CalendarEntry> {

	/**
	 * Compares two Calendar entries by comparing their dates. If both beans are of the same class
	 * and implement comparable, their native ordering will be used.
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(CalendarEntry e1, CalendarEntry e2) {
		if ((e1 instanceof Comparable) && (e2.getClass() == e1.getClass()))
			return ((Comparable<CalendarEntry>) e1).compareTo(e2);
		
		return e1.getDate().compareTo(e2.getDate());
	}
}