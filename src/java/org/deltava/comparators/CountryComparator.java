// Copyright 2010, 2106 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import org.deltava.beans.schedule.Country;

/**
 * A Comparator for Country beans.
 * @author Luke
 * @version 7.0
 * @since 3.2
 */

public class CountryComparator extends AbstractComparator<Country> {

	public static final int CODE = 0;
	public static final int NAME = 1;
	
	private static final String[] TYPES = {"Code", "Name"};
	
    /**
     * Creates a new CountryComparator with a given comparison type.
     * @param comparisonType The criteria by which to compare
     * @throws IllegalArgumentException if the type is invalid
     * @see AbstractComparator#setComparisonType(int)
     */
    public CountryComparator(int comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
    }

    /**
     * Creates a new CountryComparator with a given comparison type.
     * @param comparisonType The criteria type name by which to compare
     * @throws IllegalArgumentException if the type name is invalid
     * @see AbstractComparator#setComparisonType(String)
     */
    public CountryComparator(String comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
    }
	
    /**
     * Compares two countries by the designated criteria.
     */
    @Override
	public int compareImpl(Country c1, Country c2) {
    	switch (_comparisonType) {
    	case NAME:
    		return c1.getName().compareTo(c2.getName());
    		
    	case CODE:
    	default:
    		return c1.getCode().compareTo(c2.getCode());
    	}
    }
}