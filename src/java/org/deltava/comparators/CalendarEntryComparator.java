// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import java.util.Comparator;

import org.deltava.beans.CalendarEntry;

/**
 * A Comparator for CalendarEntry beans.
 * @author Luke
 * @version 3.1
 * @since 3.1
 */

public class CalendarEntryComparator implements Comparator<CalendarEntry>, java.io.Serializable {
	
	private boolean _isReverse;

	/**
	 * Creates a new Comparator.
	 */
	public CalendarEntryComparator() {
		this(false);
	}
	
	/**
	 * Creates a new Comparator with an optional reverse sort.
	 * @param isReverse TRUE if a reverse sort, otherwise FALSE
	 */
	public CalendarEntryComparator(boolean isReverse) {
		super();
		_isReverse = isReverse; 
	}

	/**
	 * Compares two CalendarEntry beans by comparing their dates.
	 */
	public int compare(CalendarEntry ce1, CalendarEntry ce2) {
		int tmpResult = ce1.getDate().compareTo(ce2.getDate());
		return _isReverse ? (tmpResult * -1) : tmpResult;
	}
}