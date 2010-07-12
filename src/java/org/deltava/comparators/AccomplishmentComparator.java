// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import org.deltava.beans.stats.Accomplishment;

/**
 * A comparator for Accomplishment beans. 
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class AccomplishmentComparator extends AbstractComparator<Accomplishment> {

    public static final int UNIT = 0;
    public static final int VALUE = 1;
    public static final int NAME = 2;
    public static final int PILOTS = 3;
	
    private static final String[] TYPES = {"Units", "Value", "Name", "Pilots"};
    
    /**
     * Creates a new AccomplishmentComparator with a given comparison type.
     * @param comparisonType The criteria by which to compare
     * @throws IllegalArgumentException if the type is invalid
     * @see AbstractComparator#setComparisonType(int)
     */
    public AccomplishmentComparator(int comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
    }

    /**
     * Creates a new AccomplishmentComparator with a given comparison type.
     * @param comparisonType The criteria type name by which to compare
     * @throws IllegalArgumentException if the type name is invalid
     * @see AbstractComparator#setComparisonType(String)
     */
    public AccomplishmentComparator(String comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
    }
    
    /**
     * Compares two accomplishments by the designated criteria.
     * @see java.util.Comparator#compare(Object, Object)
     */
    public int compareImpl(Accomplishment a1, Accomplishment a2) {
    	switch (_comparisonType) {
    	case UNIT:
    		return a1.getUnit().compareTo(a2.getUnit());
    	case VALUE:
    		return Integer.valueOf(a1.getValue()).compareTo(Integer.valueOf(a2.getValue()));
    	case NAME:
    		return a1.getName().compareTo(a2.getName());
    	case PILOTS:
    		return Integer.valueOf(a1.getPilots()).compareTo(Integer.valueOf(a2.getPilots()));
    	default:
    		return a1.compareTo(a2);
    	}
    }
}