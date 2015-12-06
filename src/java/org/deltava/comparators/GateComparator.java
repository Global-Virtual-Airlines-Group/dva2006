// Copyright 2012, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import org.deltava.beans.navdata.Gate;

/**
 * A comparator for airport Gates.
 * @author Luke
 * @version 6.3
 * @since 5.1
 */

public class GateComparator extends AbstractComparator<Gate> {

	public static final int NAME = 0;
	public static final int TYPENUMBER = 1;
	public static final int USAGE = 2;
	
	private static final String[] TYPES = {"Name", "Type/Number", "Populairty"};
	
    /**
     * Creates a new GateComparator with a given comparison type.
     * @param comparisonType The criteria by which to compare
     * @throws IllegalArgumentException if the type is invalid
     * @see AbstractComparator#setComparisonType(int)
     */
	public GateComparator(int comparisonType) {
		super(TYPES);
		setComparisonType(comparisonType);
	}
	
    /**
     * Compares two gates by the designated criteria.
     */
	@Override
    public int compareImpl(Gate g1, Gate g2) {
    	int tmpResult = 0;
    	switch (_comparisonType) {
    		case TYPENUMBER:
    			tmpResult = g1.getGateType().compareTo(g2.getGateType());
    			return (tmpResult == 0) ? Integer.valueOf(g1.getGateNumber()).compareTo(Integer.valueOf(g2.getGateNumber())) : tmpResult;
    			
    		case USAGE:
    			tmpResult = Integer.valueOf(g1.getUseCount()).compareTo(Integer.valueOf(g2.getUseCount()));
    			if (tmpResult != 0)
    				return tmpResult;
    	
    		//$FALL-THROUGH$
		case NAME:
    		default:
    			tmpResult = g1.getCode().compareTo(g2.getCode());
    			return (tmpResult == 0) ? g1.getName().compareTo(g2.getName()) : tmpResult;
    	}
    }
}