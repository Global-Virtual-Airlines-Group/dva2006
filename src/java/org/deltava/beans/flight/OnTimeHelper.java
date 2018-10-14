// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.time.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

/**
 * A utility class to calculate on-time statistics for a flight.
 * @author Luke
 * @version 8.4
 * @since 8.4
 */

@Helper(OnTime.class)
public class OnTimeHelper {

	private final Collection<ScheduleEntry> _flights = new ArrayList<ScheduleEntry>();
	private int _depToleranceMinutes = 60;
	
	private ScheduleEntry _entry;
	
	private static class ClosestFlight implements Comparator<FlightTimes> {
		private final ZonedDateTime _srcTime;
		
		ClosestFlight(FlightTimes ft) {
			super();
			_srcTime = ft.getTimeD();
		}

		@Override
		public int compare(FlightTimes ft1, FlightTimes ft2) {
			Duration d1 = Duration.between(ft1.getTimeD(), _srcTime).abs();
			Duration d2 = Duration.between(ft2.getTimeD(), _srcTime).abs();
			return d1.compareTo(d2);
		}
	}
	
	/**
	 * Initializes the helper.
	 * @param entries a Collection of ScheduleEntry beans
	 */
	public OnTimeHelper(Collection<ScheduleEntry> entries) {
		super();
		_flights.addAll(entries);
	}
	
	/**
	 * Returns the ScheduleEntry used to calculate timeliness.
	 * @return a ScheduleEntry, or null if none meeting tolerances
	 */
	public ScheduleEntry getScheduleEntry() {
		return _entry;
	}
	
	/**
	 * Sets the tolerance for finding a possible departure flight. 
	 * @param minutes the tolerance in minutes
	 */
	public void setDepartureTolerance(int minutes) {
		_depToleranceMinutes = Math.max(1,  minutes);
	}
	
	/**
	 * Determines whether a flight was on time.
	 * @param fr a FlightTimes object
	 * @return an OnTime enumeration
	 */
	public OnTime validate(FlightTimes fr) {

		if ((fr.getTimeD() == null) || (fr.getTimeA() == null))
			return OnTime.UNKNOWN;
		
		LocalDate dld = fr.getTimeD().toLocalDate(); LocalDate ald = fr.getTimeA().toLocalDate();
		for (ScheduleEntry se : _flights) {
			se.setTimeD(LocalDateTime.of(dld, se.getTimeD().toLocalTime()));
			se.setTimeA(LocalDateTime.of(ald, se.getTimeA().toLocalTime()));
		}
		
		// See if we match the exact flight num, otherwise sort based on closest
		Optional<ScheduleEntry> ose = _flights.stream().filter(se -> se.equals(fr) && filter(se, fr)).findFirst();
		if (!ose.isPresent()) {
			SortedSet<ScheduleEntry> entries = new TreeSet<ScheduleEntry>(new ClosestFlight(fr));
			entries.addAll(_flights);
			ose = entries.stream().filter(se -> filter(se, fr)).findFirst();
		}
		
		if (!ose.isPresent())
			return OnTime.UNKNOWN;

		// Get time delta
		_entry = ose.get();
		Duration d = Duration.between(_entry.getTimeA(), fr.getTimeA());
		if (d.isNegative())
			return OnTime.EARLY;
		
		return (d.abs().toMinutes() > 5) ? OnTime.LATE : OnTime.ONTIME;
	}
	
	private boolean filter(FlightTimes se, FlightTimes fr) {
		long dMin = Duration.between(se.getTimeD(), fr.getTimeD()).toMinutes();
		return (dMin > (_depToleranceMinutes / -2)) && (dMin < _depToleranceMinutes);
	}
}