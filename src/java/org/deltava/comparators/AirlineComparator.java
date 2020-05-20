// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import org.deltava.beans.schedule.Airline;

/**
 * A comparator for Airline beans.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class AirlineComparator extends AbstractComparator<Airline> {
	
	public static final int CODE = 0;
    public static final int NAME = 1;
    
    private static final String[] TYPES = {"Code", "Airline Name"};
    
    /**
     * Creates a new AirlineComparator with a given comparison type.
     * @param comparisonType The criteria by which to compare
     * @throws IllegalArgumentException if the type is invalid
     * @see AbstractComparator#setComparisonType(int)
     */
    public AirlineComparator(int comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
    }

    /**
     * Creates a new AirlineComparator with a given comparison type.
     * @param comparisonType The criteria type name by which to compare
     * @throws IllegalArgumentException if the type name is invalid
     * @see AbstractComparator#setComparisonType(String)
     */
    public AirlineComparator(String comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
    }

	@Override
	protected int compareImpl(Airline a1, Airline a2) {
		switch (_comparisonType) {
		case NAME:
			return a1.getName().compareTo(a2.getName());
			
		default:
			return a1.getCode().compareTo(a2.getCode());
		}
	}
}