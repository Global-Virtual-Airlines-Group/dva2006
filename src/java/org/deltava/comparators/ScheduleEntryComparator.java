// Copyright 2011, 2015, 2016, 2017, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import org.deltava.beans.schedule.*;

/**
 * A comparator for Schedule entries.
 * @author Luke
 * @version 11.0
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
	public static final int FLIGHT_DTIME = 11;
	public static final int ROUTE_FLIGHT = 12;
	
	private static final String[] TYPES = {"???", "Flight Number", "Equipment Type", "Origin", "Destination", "Departure Time", "Arrival Time", "Length", "Distance", "Flight Count", "Last Flown", "Flight+Departure", "Route+Flight"};
	
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
	
	@Override
	protected int compareImpl(ScheduleEntry se1, ScheduleEntry se2) {
		switch (_comparisonType) {
			case EQTYPE:
				int tmpResult = se1.getEquipmentType().compareTo(se2.getEquipmentType());
				return (tmpResult == 0) ? se1.compareTo(se2) : tmpResult;
				
			case ORIGIN:
				tmpResult = se1.getAirportD().compareTo(se2.getAirportD());
				return (tmpResult == 0) ? se1.compareTo(se2) : tmpResult;
				
			case DEST:
				tmpResult = se1.getAirportA().compareTo(se2.getAirportA());
				return (tmpResult == 0) ? se1.compareTo(se2) : tmpResult;
				
			case DTIME:
				tmpResult = se1.getTimeD().compareTo(se2.getTimeD());
				return (tmpResult == 0) ? se1.compareTo(se2) : tmpResult;
				
			case ATIME:
				tmpResult = se1.getTimeA().compareTo(se2.getTimeA());
				return (tmpResult == 0) ? se1.compareTo(se2) : tmpResult;
				
			case LENGTH:
				tmpResult = Integer.compare(se1.getLength(), se2.getLength());
				return (tmpResult == 0) ? se1.compareTo(se2) : tmpResult;
				
			case DISTANCE:
				tmpResult = Integer.compare(se1.getDistance(), se2.getDistance());
				return (tmpResult == 0) ? se1.compareTo(se2) : tmpResult;
				
			case FLCOUNT:
				if ((se1 instanceof ScheduleSearchEntry sse1) && (se2 instanceof ScheduleSearchEntry sse2))
					return Integer.compare(sse1.getFlightCount(), sse2.getFlightCount());
				
				return se1.compareTo(se2);
				
			case FLIGHT_DTIME:
				tmpResult = se1.compareTo(se2);
				return (tmpResult == 0) ? se1.getTimeD().toInstant().compareTo(se2.getTimeD().toInstant()) : tmpResult;
				
			case LASTFLT:
				if ((se1 instanceof ScheduleSearchEntry sse1) && (se2 instanceof ScheduleSearchEntry sse2)) {
					if (sse2.getLastFlownOn() == null)
						return (sse1.getLastFlownOn() == null) ? 0 : -1;
					
					return (sse1.getLastFlownOn() == null) ? -1 : sse1.getLastFlownOn().compareTo(sse2.getLastFlownOn());
				}
				
				return se1.compareTo(se2);
				
			case ROUTE_FLIGHT:
				tmpResult = se1.getAirportD().compareTo(se2.getAirportD());
				if (tmpResult == 0)
					tmpResult = se1.getAirportA().compareTo(se2.getAirportA());
				
				return (tmpResult == 0) ? se1.compareTo(se2) : tmpResult;

			case FLIGHT:
			default:
				return se1.compareTo(se2);
		}
	}
}