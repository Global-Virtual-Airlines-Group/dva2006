// Copyright 2011, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import org.deltava.beans.schedule.*;

/**
 * A comparator for Schedule entries.
 * @author Luke
 * @version 8.0
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
	public static final int FLCOUNT = 9;
	public static final int LASTFLT = 10;
	
	private static final String[] TYPES = {"???", "Flight Number", "Equipment Type", "Origin", "Destination", "Departure Time", "Arrival Time", "Length", "Distance", "Flight Count", "Last Flown"};
	
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
	@Override
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
				return Integer.compare(se1.getLength(), se2.getLength());
				
			case DISTANCE:
				return Integer.compare(se1.getDistance(), se2.getDistance());
				
			case FLCOUNT:
				if ((se1 instanceof ScheduleSearchEntry) && (se2 instanceof ScheduleSearchEntry)) {
					ScheduleSearchEntry sse1 = (ScheduleSearchEntry) se1;
					ScheduleSearchEntry sse2 = (ScheduleSearchEntry) se2;
					return Integer.compare(sse1.getFlightCount(), sse2.getFlightCount());
				}
				
				return se1.compareTo(se2);
				
			case LASTFLT:
				if ((se1 instanceof ScheduleSearchEntry) && (se2 instanceof ScheduleSearchEntry)) {
					ScheduleSearchEntry sse1 = (ScheduleSearchEntry) se1;
					ScheduleSearchEntry sse2 = (ScheduleSearchEntry) se2;
					if (sse2.getLastFlownOn() == null)
						return (sse1.getLastFlownOn() == null) ? 0 : -1;
					
					return (sse1.getLastFlownOn() == null) ? -1 : sse1.getLastFlownOn().compareTo(sse2.getLastFlownOn());
				}
				
				return se1.compareTo(se2);

			case FLIGHT:
			default:
				return se1.compareTo(se2);
		}
	}
}