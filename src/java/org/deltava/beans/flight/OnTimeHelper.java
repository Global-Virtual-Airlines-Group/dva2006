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
	
	private ScheduleEntry getClosestScheduleEntry(FlightTimes ft) {
		LocalDate dld = ft.getTimeD().toLocalDate(); LocalDate ald = (ft.getTimeA() == null) ? dld : ft.getTimeA().toLocalDate();
		_flights.forEach(se -> { se.setTimeD(LocalDateTime.of(dld, se.getTimeD().toLocalTime())); se.setTimeA(LocalDateTime.of(ald, se.getTimeA().toLocalTime())); });
		
		Optional<ScheduleEntry> ose = _flights.stream().filter(se -> se.equals(ft) || filter(se.getTimeD(), ft.getTimeD())).findFirst();
		if (!ose.isPresent()) {
			SortedSet<ScheduleEntry> entries = new TreeSet<ScheduleEntry>(new ClosestFlight(ft));
			entries.addAll(_flights);
			ose = entries.stream().filter(se -> filter(se.getTimeD(), ft.getTimeD())).findFirst();
		}
		
		return ose.orElse(null);
	}
		
	/**
	 * Determines whether a flight was on time.
	 * @param fr a FlightTimes object
	 * @return an OnTime enumeration
	 */
	public OnTime validate(FlightTimes fr) {
		if ((fr.getTimeD() == null) || (fr.getTimeA() == null))
			return OnTime.UNKNOWN;

		// Get the closest schedule entry
		_entry = getClosestScheduleEntry(fr);
		if (_entry == null)
			return OnTime.UNKNOWN;

		Duration d = Duration.between(_entry.getTimeA(), fr.getTimeA());
		if (!d.isNegative() && !d.isZero())
			return OnTime.LATE;
		
		return (d.abs().toMinutes() > 10) ? OnTime.EARLY: OnTime.ONTIME;
	}
	
	/**
	 * Determines whether a flight is departing on time.
	 * @param fr a FlightTimesl object
	 * @return an OnTime enumeration
	 */
	public OnTime validateDeparture(FlightTimes fr) {
		if (fr.getTimeD() == null)
			return OnTime.UNKNOWN;
		
		// Get the closest schedule entry
		_entry = getClosestScheduleEntry(fr);
		if (_entry == null)
			return OnTime.UNKNOWN;
		
		Duration d = Duration.between(_entry.getTimeD(), fr.getTimeD());
		if (!d.isNegative() && !d.isZero())
			return OnTime.LATE;
		
		return (d.abs().toMinutes() > 10) ? OnTime.EARLY: OnTime.ONTIME;
	}
	
	private boolean filter(ZonedDateTime scheduledDeparture, ZonedDateTime actualDeparture) {
		long dMin = Duration.between(scheduledDeparture, actualDeparture).toMinutes();
		return (dMin > (_depToleranceMinutes / -2)) && (dMin < _depToleranceMinutes);
	}
}