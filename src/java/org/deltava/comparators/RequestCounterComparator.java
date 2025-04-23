// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import org.deltava.beans.system.RequestCounter;

/**
 * A comparator for request counters. 
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class RequestCounterComparator extends AbstractComparator<RequestCounter> {
	
	public static final int ADDR = 0;
	public static final int REQUESTS = 1;
	public static final int OLDEST = 2;
	public static final int NEWEST = 3;
	
	private static final String[] TYPES = {"Address", "Requests", "Oldest Request", "Newest Request"};
	
    /**
     * Creates a new comparator with a given comparison type.
     * @param comparisonType The criteria by which to compare
     * @throws IllegalArgumentException if the type is invalid
     * @see AbstractComparator#setComparisonType(int)
     */
    public RequestCounterComparator(int comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
    }

    /**
     * Creates a new comparator with a given comparison type.
     * @param comparisonType The criteria name by which to compare
     * @throws IllegalArgumentException if the type name is invalid
     * @see AbstractComparator#setComparisonType(String)
     */
    public RequestCounterComparator(String comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
    }

	@Override
	protected int compareImpl(RequestCounter rc1, RequestCounter rc2) {
		switch (_comparisonType) {
			case REQUESTS:
				int tmpResult = Integer.compare(rc1.getRequests(), rc2.getRequests());
				return (tmpResult == 0) ? rc1.getAddress().compareTo(rc2.getAddress()) : tmpResult;
			case OLDEST:
				return rc1.getOldest().compareTo(rc2.getOldest());
			case NEWEST:
				return rc2.getNewest().compareTo(rc2.getNewest());
			default:
				return rc1.getAddress().compareTo(rc2.getAddress());

		}
	}
}