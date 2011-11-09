// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import org.deltava.beans.schedule.ScheduleEntry;

/**
 * A comparator for Schedule entries.
 * @author Luke
 * @version 4.1
 * @since 4.1
 */

public class ScheduleEntryComparator extends AbstractComparator<ScheduleEntry> {

	public static final int FLIGHT = 1;
	public static final int EQTYPE = 2;
	public static final int ORIGIN = 3;
	public static final int DEST = 4;
	public static final int DTIME = 5;
	public static final int ATIME = 6;
	public static final int LENGTH = 7;
	public static final int DISTANCE = 8;
	
	private static final String[] TYPES = {"???", "Flight Number", "Equipment Type", "Origin", "Destination",
		"Departure Time", "Arrival Time", "Length", "Distance"};
	
    /**
     * Creates a new comparator with a given comparison type.
     * @param comparisonType The criteria by which to compare
     * @throws IllegalArgumentException if the type is invalid
     * @see AbstractComparator#setComparisonType(int)
     */
	public ScheduleEntryComparator(int comparisonType) {
		super(TYPES);
		setComparisonType(comparisonType);
	}
	
    /**
     * Compares two schedule entries by the designated criteria.
     */
	protected int compareImpl(ScheduleEntry se1, ScheduleEntry se2) {
		switch (_comparisonType) {
			case EQTYPE:
				return se1.getEquipmentType().compareTo(se2.getEquipmentType());
				
			case ORIGIN:
				return se1.getAirportD().compareTo(se2.getAirportD());
				
			case DEST:
				return se1.getAirportA().compareTo(se2.getAirportA());
				
			case DTIME:
				return se1.getTimeD().compareTo(se2.getTimeD());
				
			case ATIME:
				return se1.getTimeA().compareTo(se2.getTimeA());
				
			case LENGTH:
				return Integer.valueOf(se1.getLength()).compareTo(Integer.valueOf(se2.getLength()));
				
			case DISTANCE:
				return Integer.valueOf(se1.getDistance()).compareTo(Integer.valueOf(se2.getDistance()));

			case FLIGHT:
			default:
				int tmpResult = se1.getAirline().compareTo(se2.getAirline());
				if (tmpResult  == 0)
					tmpResult = Integer.valueOf(se1.getFlightNumber()).compareTo(Integer.valueOf(se2.getFlightNumber()));
				return (tmpResult == 0) ? Integer.valueOf(se1.getLeg()).compareTo(Integer.valueOf(se2.getLeg())) : tmpResult;
		}
	}
}