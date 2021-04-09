// Copyright 2005, 2006, 2009, 2012, 2016, 2017, 2018, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.Airport;

/**
 * A comparator to sort Flight Reports.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class FlightReportComparator extends AbstractComparator<FlightReport> {

    public static final int DATE = 0;
    public static final int LENGTH = 1;
    public static final int DISTANCE = 2;
    public static final int EQUIPMENT = 3;
    public static final int ORIGIN = 4;
    public static final int DESTINATION = 5;
    public static final int FLIGHTCODE = 6;
    public static final int SUBMISSION = 7;

    private static final String[] TYPES = { "Date", "Length", "Distance", "Equipment Type", "Origin", "Destination", "Flight Code", "Submission Date" };

    /**
     * Creates a new FlightReport comparator with a particular comparison type code.
     * @param comparisonType The criteria by which to compare
     * @throws IllegalArgumentException if the type is invalid
     * @see AbstractComparator#setComparisonType(int)
     */
    public FlightReportComparator(int comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
    }

    /**
     * Creates a new FlightReport comparator with a particular comparison type name.
     * @param typeName The criteria type name by which to compare
     * @throws IllegalArgumentException if the type name is invalid
     * @see AbstractComparator#setComparisonType(String)
     */
    public FlightReportComparator(String typeName) {
        super(TYPES);
        setComparisonType(typeName);
    }

    /**
     * Compares two Flight Reports by the designated criterial.
     * @throws ClassCastException if either object is not a FlightReport bean
     * @see java.util.Comparator#compare(Object, Object)
     */
    @Override
	protected int compareImpl(FlightReport f1, FlightReport f2) {
    	int tmpResult;
        switch (_comparisonType) {
            case DATE:
                if (f1.getStatus() == FlightStatus.DRAFT)
                    return -1;
                if (f2.getStatus() == FlightStatus.DRAFT)
                    return 1;

                tmpResult = f1.getDate().compareTo(f2.getDate());
                return (tmpResult == 0) ? f1.compareTo(f2) : tmpResult;

            case LENGTH:
                tmpResult = Integer.compare(f1.getLength(), f2.getLength());
                return (tmpResult == 0) ? f1.compareTo(f2) : tmpResult;

            case DISTANCE:
                tmpResult = Integer.compare(f1.getDistance(), f2.getDistance());
                return (tmpResult == 0) ? f1.compareTo(f2) : tmpResult;

            case EQUIPMENT:
                tmpResult = f1.getEquipmentType().compareTo(f2.getEquipmentType());
                return (tmpResult == 0) ? f1.compareTo(f2) : tmpResult;

            case ORIGIN:
                Airport aO = f1.getAirportD();
                tmpResult = (aO == null) ? -1 : aO.compareTo(f2.getAirportD());
                if (tmpResult != 0)
                    return tmpResult;
                
                // If the origin is equal, compare the destination airports
                Airport aD = f1.getAirportA();
                return (aD == null) ? -1 : aD.compareTo(f2.getAirportA());

            case DESTINATION:
                aD = f1.getAirportA();
                tmpResult = (aD == null) ? -1 : aD.compareTo(f2.getAirportA());
                if (tmpResult != 0)
                    return tmpResult;

                // If the destination is equal, compare the origin airports
                aO = f1.getAirportD();
                return (aO == null) ? -1 : aO.compareTo(f2.getAirportD());
                
            case SUBMISSION:
            	if (f1.getSubmittedOn() == null)
            		return -1;
            	if (f2.getSubmittedOn() == null)
            		return 1;
            	
            	tmpResult = f1.getSubmittedOn().compareTo(f2.getSubmittedOn());
            	return (tmpResult == 0) ? Integer.compare(f1.getID(), f2.getID()) : tmpResult;
                
            case FLIGHTCODE:
            default:
                tmpResult = f1.compareTo(f2);
                return (tmpResult == 0) ? Integer.compare(f1.getID(), f2.getID()) : tmpResult;
        }
    }
}