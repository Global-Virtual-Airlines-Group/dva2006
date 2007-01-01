// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import org.deltava.beans.schedule.Airport;

/**
 * A comparator for Airport beans.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * @see Airport
 */

public class AirportComparator extends AbstractComparator<Airport> {

    public static final int IATA = 0;
    public static final int ICAO = 1;
    public static final int NAME = 2;
    public static final int LATITUDE = 3;
    public static final int LONGITUDE = 4;
    
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
    protected int compareImpl(Airport a1, Airport a2) {
        switch (_comparisonType) {
        	case ICAO :
        	    return a1.getICAO().compareTo(a2.getICAO());
        	case NAME :
        	    return a1.getName().compareTo(a2.getName());
        	case LATITUDE :
        	    return new Double(a1.getLatitude()).compareTo(new Double(a2.getLatitude()));
        	case LONGITUDE :
        	    return new Double(a1.getLongitude()).compareTo(new Double(a2.getLongitude()));
        	default :
        	    return a1.getIATA().compareTo(a2.getIATA());
        }
    }
}