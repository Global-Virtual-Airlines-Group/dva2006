package org.deltava.comparators;

import org.deltava.beans.FlightReport;
import org.deltava.beans.schedule.Airport;

/**
 * A class to sort Flight Reports.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public class FlightReportComparator extends AbstractComparator {

    public static final int DATE = 0;
    public static final int LENGTH = 1;
    public static final int DISTANCE = 2;
    public static final int EQUIPMENT = 3;
    public static final int ORIGIN = 4;
    public static final int DESTINATION = 5;
    public static final int PROCESSING_TIME = 6;
    public static final int FLIGHTCODE = 7;

    private static final String[] TYPES = { "Date", "Length", "Distance", "Equipment Type", "Origin", "Destination",
            "Processing Time", "Flight Code" };

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
     * Creates a new FlightReport comparator with a particular comparison type code.
     * @param typeName The criteria type name by which to compare
     * @throws IllegalArgumentException if the type name is invalid
     * @see AbstractComparator#setComparisonType(String)
     */
    public FlightReportComparator(String typeName) {
        super(TYPES);
        setComparisonType(typeName);
    }

    /**
     * Compares two equipment programs by the designated criteria
     * @throws ClassCastException if either object is not an EquipmentType
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    protected int compareImpl(Object o1, Object o2) {
        FlightReport f1 = (FlightReport) o1;
        FlightReport f2 = (FlightReport) o2;

        int tmpResult;
        Airport aO, aD;
        switch (_comparisonType) {
            case DATE:
                if (f1.getStatus() == FlightReport.DRAFT) {
                    return -1;
                } else if (f2.getStatus() == FlightReport.DRAFT) {
                    return 1;
                } else {
                    return (f1.getStatus() == FlightReport.DRAFT) ? 1 : f1.getDate().compareTo(f2.getDate());
                }

            case LENGTH:
                return new Integer(f1.getLength()).compareTo(new Integer(f2.getLength()));

            case DISTANCE:
                return new Integer(f1.getDistance()).compareTo(new Integer(f2.getDistance()));

            case EQUIPMENT:
                return f1.getEquipmentType().compareTo(f2.getEquipmentType());

            case ORIGIN:
                aO = f1.getAirportD();
                tmpResult = (aO == null) ? -1 : aO.compareTo(f2.getAirportD());
                if (tmpResult != 0)
                    return tmpResult;
                
                // If the origin is equal, compare the destination airports
                aD = f1.getAirportA();
                return (aD == null) ? -1 : aD.compareTo(f2.getAirportA());

            case DESTINATION:
                aD = f1.getAirportA();
                tmpResult = (aD == null) ? -1 : aD.compareTo(f2.getAirportA());
                if (tmpResult != 0)
                    return tmpResult;

                // If the destination is equal, compare the origin airports
                aO = f1.getAirportD();
                return (aO == null) ? -1 : aO.compareTo(f2.getAirportD());
                
            case PROCESSING_TIME:
                throw new UnsupportedOperationException();

            case FLIGHTCODE:
            default:
                return f1.compareTo(f2);
        }
    }
}