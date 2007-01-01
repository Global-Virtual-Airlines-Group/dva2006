// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import org.deltava.beans.EquipmentType;

/**
 * A comparator for EquipmentType beans.
 * @author LKolin
 * @version 1.0
 * @since 1.0
 * @see EquipmentType
 */

public class EquipmentComparator extends AbstractComparator<EquipmentType> {

    public static final int STAGE = 0;
    public static final int NAME = 1;
    
    private static final String[] TYPES = {"Stage", "Name"};
    
    /**
     * Creates a new EquipmentComparator with a given comparison type.
     * @param comparisonType The criteria by which to compare
     * @throws IllegalArgumentException if the type is invalid
     * @see AbstractComparator#setComparisonType(int)
     */
    public EquipmentComparator(int comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
    }

    /**
     * Creates a new EquipmentComparator with a given comparison type.
     * @param comparisonType The criteria type name by which to compare
     * @throws IllegalArgumentException if the type name is invalid
     * @see AbstractComparator#setComparisonType(String)
     */
    public EquipmentComparator(String comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
    }
    
    /**
     * Compares two equipment programs by the designated criteria.
     * @throws ClassCastException if either object is not an EquipmentType 
     * @see java.util.Comparator#compare(Object, Object)
     */
    protected int compareImpl(EquipmentType et1, EquipmentType et2) {
        switch (_comparisonType) {
        	case NAME :
        	    return et1.getName().compareTo(et2.getName());
        	    
        	default :
        	    return et1.compareTo(et2); // Uses default ordering
        }
    }
}