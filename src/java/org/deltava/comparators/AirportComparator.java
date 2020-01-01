// Copyright 2005, 2006, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import java.text.Collator;

import org.deltava.beans.schedule.Airport;

/**
 * A comparator for Airport beans.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class AirportComparator extends AbstractComparator<Airport> {

    public static final int IATA = 0;
    public static final int ICAO = 1;
    public static final int NAME = 2;
    public static final int LATITUDE = 3;
    public static final int LONGITUDE = 4;
    
    private final Collator _cl = Collator.getInstance();
    
    private static final String[] TYPES = {"IATA Code", "ICAO Code", "Airport Name", "Latitude", "Longitude"};
    
    /**
     * Creates a new AirportComparator with a given comparison type
     * @param comparisonType The criteria by which to compare
     * @throws IllegalArgumentException if the type is invalid
     * @see AbstractComparator#setComparisonType(int)
     */
    public AirportComparator(int comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
        _cl.setStrength(Collator.SECONDARY);
        _cl.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
    }

    /**
     * Creates a new AirportComparator with a given comparison type
     * @param comparisonType The criteria name by which to compare
     * @throws IllegalArgumentException if the type name is invalid
     * @see AbstractComparator#setComparisonType(String)
     */
    public AirportComparator(String comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
    }

    /**
     * Compares two airports by the designated criteria
     * @throws ClassCastException if either object is not an Airport
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
	protected int compareImpl(Airport a1, Airport a2) {
        switch (_comparisonType) {
        	case ICAO:
        	    return a1.getICAO().compareTo(a2.getICAO());
        	case NAME:
        		int tmpResult = _cl.compare(a1.getName(), a2.getName()); 
        	    return (tmpResult == 0) ? a1.getIATA().compareTo(a2.getIATA()) : tmpResult;
        	case LATITUDE:
        	    tmpResult = Double.compare(a1.getLatitude(), a2.getLatitude());
        	    return (tmpResult == 0) ? Double.compare(a1.getLongitude(), a2.getLongitude()) : tmpResult; 
        	case LONGITUDE:
        		tmpResult = Double.compare(a1.getLongitude(), a2.getLongitude());
        		return (tmpResult == 0) ? Double.compare(a1.getLatitude(), a2.getLatitude()) : tmpResult;
        	default:
        	    return a1.getIATA().compareTo(a2.getIATA());
        }
    }
}