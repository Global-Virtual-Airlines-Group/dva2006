// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;
import java.time.Duration;
import java.util.stream.Collectors;

import org.deltava.beans.*;

/**
 * A utility class to adjust and merge Raw Schedule entries. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

@Helper(RawScheduleEntry.class)
public class RawScheduleHelper {
	
	/**
	 * A comparator to sort based on code share flight value.
	 */
	static class CodeShareComparator implements Comparator<RawScheduleEntry> {
		@Override
		public int compare(RawScheduleEntry rse1, RawScheduleEntry rse2) {
			int tmpResult = rse1.getCodeShare().compareTo(rse2.getCodeShare());
			if (tmpResult == 0)
				tmpResult = Integer.compare(rse1.getFlightNumber(), rse2.getFlightNumber());
			
			return (tmpResult == 0) ? rse1.getAirline().compareTo(rse2.getAirline()) : tmpResult;
		}
	}
	
	/**
	 * A basic Raw Schedule Entry duplicate checker. 
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
			if (tmpResult == 0) {
				tmpResult = Boolean.compare(rse2.isCodeShare(), rse1.isCodeShare());
				if (tmpResult == 0)
					tmpResult = rse1.getCodeShare().compareTo(rse2.getCodeShare());
			}
			
			return (tmpResult == 0) ? Integer.compare(rse1.getDayMap(), rse2.getDayMap()) : tmpResult;
		}
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

	// static class
	private RawScheduleHelper() {
		super();
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
	
	/**
	 * Returns a duplicate entry checker for raw Schhedule entries.
	 * @param checkDeparture TRUE to check departure times, otherwise FALSE
	 * @return a Comparator to be passed into a Set for duplicate checking
	 */
	public static Comparator<RawScheduleEntry> getDupeChecker(boolean checkDeparture) {
		return checkDeparture ? new TimeDupeChecker(30) : new RawDupeChecker(true);
	}
	
	/**
	 * Merges code share flights, handling cases where there are duplicate entries if a flight is code shared under two different Airlines.
	 * @param entries a Collection of RawScheduleEntry beans
	 */
	public static void mergeCodeShares(SequencedCollection<RawScheduleEntry> entries) {
		List<RawScheduleEntry> csEntries = entries.stream().filter(ScheduleEntry::isCodeShare).collect(Collectors.toList());
		csEntries.sort(new CodeShareComparator());
		if (csEntries.size() < 2) return;
		
		// Remove the first entry, it by definition cannot be a dupe
		RawScheduleEntry lastCS = csEntries.getFirst();
		csEntries.removeFirst();
		
		// Add list of dupes, and merge codeshare data
		Collection<RawScheduleEntry> dupes = new HashSet<RawScheduleEntry>();
		for (RawScheduleEntry rse : csEntries) {
			if (!rse.matches(lastCS) || FlightNumber.compare(rse, lastCS, false) != 0) {
				lastCS = rse;
				continue;
			}
			
			// Use the codeshare field as an accumulating buffer
			StringBuilder buf = new StringBuilder(lastCS.getCodeShare());
			buf.append(',').append(rse.getCodeShare());
			lastCS.setCodeShare(buf.toString());
			dupes.add(rse);
		}
		
		// Remove dupes and format the entries
		entries.removeAll(dupes);
		for (RawScheduleEntry rse : csEntries) {
			if (rse.getCodeShare().indexOf(',') == -1) continue;
			rse.setRemarks(String.format("Multiple code shares (%s)", rse.getCodeShare()));
			rse.setCodeShare("MULTI");
		}
	}
}