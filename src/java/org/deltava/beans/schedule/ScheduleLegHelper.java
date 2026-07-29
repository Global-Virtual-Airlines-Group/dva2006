// Copyright 2020, 2025, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;
import java.util.stream.*;
import java.time.Duration;

import org.deltava.beans.Helper;
import org.deltava.comparators.ScheduleEntryComparator;

/**
 * A utility class to assign leg numbers to schedule entries. 
 * @author Luke
 * @version 12.5
 * @since 9.0
 */

@Helper(ScheduleEntry.class)
public class ScheduleLegHelper {
	
	private static final Comparator<ScheduleEntry> SRT = new ScheduleEntryComparator(ScheduleEntryComparator.ATIME);
	
	/**
	 * A basic raw Schedule Entry duplicate checker. 
	 */
	static class RawDupeChecker implements Comparator<RawScheduleEntry> {
		private final boolean _checkFlightNumber;
		
		protected RawDupeChecker(boolean checkFlightNumber) {
			super();
			_checkFlightNumber = checkFlightNumber;
		}

		@Override
		public int compare(RawScheduleEntry rse1, RawScheduleEntry rse2) {
			
			int tmpResult = rse1.getAirportD().compareTo(rse2.getAirportD());
			if (tmpResult == 0)
				tmpResult = rse1.getAirportA().compareTo(rse2.getAirportA());
			if ((tmpResult == 0) && _checkFlightNumber)
				tmpResult = rse1.compareTo(rse2);
			if (tmpResult == 0)
				tmpResult = rse1.getStartDate().compareTo(rse2.getStartDate());
			if (tmpResult == 0)
				tmpResult = rse1.getEndDate().compareTo(rse2.getEndDate());
			
			return (tmpResult == 0) ? Integer.compare(rse1.getDayMap(), rse2.getDayMap()) : tmpResult;
		}
	}

	// static class
	private ScheduleLegHelper() {
		super();
	}

	/**
	 * A Raw Schedule Entry duplicate checker to strip out flights with similar routes and departure times.
	 */
	static class TimeDupeChecker extends RawDupeChecker {
		private final int _delta;
		
		protected TimeDupeChecker(int delta) {
			super(false);
			_delta = delta;
		}
		
		@Override
		public int compare(RawScheduleEntry rse1, RawScheduleEntry rse2) {
			
			int tmpResult = super.compare(rse1, rse2);
			if (tmpResult != 0) return tmpResult;
			
			Duration d = Duration.between(rse1.getTimeD().toLocalTime(), rse2.getTimeD().toLocalTime());
			long timeDelta = d.abs().toMinutes();
			return (timeDelta <= _delta) ? 0 : (d.isPositive() ? -1 : 1);
		}
	}
	
	/**
	 * Returns a duplicate entry checker for raw Schhedule entries.
	 * @param checkDeparture TRUE to check departure times, otherwise FALSE
	 * @return a Comparator to be passed into a Set for duplicate checking
	 */
	public static Comparator<RawScheduleEntry> getDupeChecker(boolean checkDeparture) {
		return checkDeparture ? new TimeDupeChecker(30) : new RawDupeChecker(true);
	}

	/**
	 * Calculates Leg numbers for flights with duplicate Flight numbers.
	 * @param entries the ScheduleEntries to check
	 * @return a Collection of ScheduleEntries
	 */
	public static <T extends ScheduleEntry> Collection<T> calculateLegs(Collection<T> entries) {
		
		// Divide into buckets based on flight number
		Map<String, List<T>> fMap = new HashMap<String, List<T>>();
		entries.stream().forEach(se -> addEntry(fMap, se.getShortCode(), se));
		
		// Sort each bucket based on arrival time
		return fMap.entrySet().stream().map(Map.Entry::getValue).flatMap(ScheduleLegHelper::setLegs).collect(Collectors.toList());
	}

	/**
	 * Calculates synthetic Line Numbers from a collection of RawScheduleEntries. 
	 * @param entries the Collection of RawScheduleEntry beans
	 */
	public static void calculateLineNumbers(SequencedCollection<RawScheduleEntry> entries) {
		int ln = 0;
		for (RawScheduleEntry rse : entries)
			rse.setLineNumber(++ln);
	}

	/*
	 * Helper method to create map of collections.
	 */
	private static <T extends ScheduleEntry> void addEntry(Map<String, List<T>> m, String key, T value) {
		List<T> c = m.get(key);
		if (c == null) {
			c = new ArrayList<T>();
			m.put(key, c);
		}
		
		c.add(value);
	}
	
	/*
	 * Sort schedule entry bucket and assign leg numbers.
	 */
	private static <T extends ScheduleEntry> Stream<T> setLegs(List<T> entries) {
		List<T> e2 =  (entries.size() > 6) ? entries.subList(0, 6) : entries;
		Collections.sort(e2, SRT); int leg = 0;
		for (ScheduleEntry se : e2)
			se.setLeg(++leg);
		
		return e2.stream();
	}
}