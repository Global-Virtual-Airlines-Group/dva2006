// Copyright 2018, 2019, 2021, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.time.*;
import java.util.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

/**
 * A utility class to calculate on-time statistics for a flight.
 * @author Luke
 * @version 11.6
 * @since 8.4
 */

@Helper(OnTime.class)
public class OnTimeHelper {
	
	private static final Logger log = LogManager.getLogger(OnTimeHelper.class);

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
	 * Returns the departure time tolerance limit.
	 * @return the limit in minutes
	 */
	public int getTolerance() {
		return _depToleranceMinutes;
	}
	
	/**
	 * Returns if the helper has any flights to check against.
	 * @return TRUE if flights or schedule entries have been loaded, otherwise FALSE
	 */
	public boolean hasFlights() {
		return (_flights.size() > 0);
	}
	
	/**
	 * Adds a Draft Flight Report to calculate timeliness. This is used for situations where a Flight Assignment was generated on a different
	 * day and the schedule no longer contains the same flights. 
	 * @param dfr a DraftFlightReport
	 */
	public void add(DraftFlightReport dfr) {
		if (!dfr.hasFlightTimes()) return;
		ScheduleEntry se = new ScheduleEntry(dfr.getAirline(), dfr.getFlightNumber(), dfr.getLeg());
		se.setEquipmentType(dfr.getEquipmentType());
		se.setAirportD(dfr.getAirportD());
		se.setAirportA(dfr.getAirportA());
		se.setTimeD(dfr.getTimeD().toLocalDateTime());
		se.setTimeA(dfr.getTimeA().toLocalDateTime());
		se.setSource(ScheduleSource.DRAFT);
		_flights.add(se);
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
		
		// Filter for matching flight # and close range
		if (ft instanceof FlightNumber fn) {
			Optional<ScheduleEntry> ose = _flights.stream().filter(se -> (FlightNumber.compare(se, fn, false) == 0) && matchDeparture(se, ft.getTimeD())).findAny();
			if (ose.isPresent())
				return ose.get();
		}
		
		Optional<ScheduleEntry> ose = _flights.stream().sorted(new ClosestFlight(ft)).filter(se -> matchDeparture(se, ft.getTimeD())).findFirst();
		return ose.orElse(null);
	}
		
	/**
	 * Determines whether a flight was on time.
	 * @param fr a FlightTimes object
	 * @return an OnTime enumeration
	 */
	public OnTime validate(FlightTimes fr) {
		if (!fr.hasFlightTimes()) return OnTime.UNKNOWN;

		// Get the closest schedule entry
		_entry = getClosestScheduleEntry(fr);
		if (_entry == null)
			return OnTime.UNKNOWN;
		
		// Adjust the arrival time by one minute
		Duration d = Duration.between(_entry.getTimeA().plusMinutes(1), fr.getTimeA());
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
		
		Duration d = Duration.between(_entry.getTimeD().plusMinutes(1), fr.getTimeD());
		if (!d.isNegative() && !d.isZero())
			return OnTime.LATE;
		
		return (d.abs().toMinutes() > 10) ? OnTime.EARLY: OnTime.ONTIME;
	}
	
	private boolean matchDeparture(ScheduleEntry se, ZonedDateTime actualDeparture) {
		int earlyLimit = _depToleranceMinutes / -2; int lateLimit = _depToleranceMinutes;
		long dMin = Duration.between(se.getTimeD(), actualDeparture).toMinutes();
		log.info("{} {} delta = {}, ({} .. {})", se.getFlightCode(), se.getTimeD(), Long.valueOf(dMin), Integer.valueOf(earlyLimit), Integer.valueOf(lateLimit));
		return (dMin > earlyLimit) && (dMin < lateLimit);
	}
}