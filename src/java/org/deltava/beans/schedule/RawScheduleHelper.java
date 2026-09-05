// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;
import java.time.Duration;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;

/**
 * A utility class to adjust and merge Raw Schedule entries. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

@Helper(RawScheduleEntry.class)
public class RawScheduleHelper {
	
	private static final Logger log = LogManager.getLogger(RawScheduleHelper.class);
	
	/**
	 * A comparator to sort based on soruce and line number.
	 */
	static class LineComparator implements Comparator<RawScheduleEntry> {
		@Override
		public int compare(RawScheduleEntry rse1, RawScheduleEntry rse2) {
			int tmpResult = ScheduleSource.comparator().compare(rse1.getSource(), rse2.getSource());
			return (tmpResult == 0) ? Integer.compare(rse1.getLineNumber(), rse2.getLineNumber()) : tmpResult;
		}
	}
	
	/**
	 * A comparator to sort based on code share flight value.
	 */
	static class CodeShareComparator implements Comparator<RawScheduleEntry> {
		@Override
		public int compare(RawScheduleEntry rse1, RawScheduleEntry rse2) {
			int tmpResult = rse1.getAirline().compareTo(rse2.getAirline());
			if (tmpResult == 0)
				tmpResult = Integer.compare(rse1.getFlightNumber(), rse2.getFlightNumber());
			if (tmpResult == 0)
				tmpResult = Boolean.compare(rse2.isCodeShare(), rse1.isCodeShare());
			return ((tmpResult == 0) && rse1.isCodeShare()) ? rse1.getCodeShare().compareTo(rse2.getCodeShare()) : tmpResult;
		}
	}
	
	/**
	 * A comparator to sort based on route pair and departure/arrival times.
	 */
	static class FlightTimesComparator implements Comparator<RawScheduleEntry> {
		@Override
		public int compare(RawScheduleEntry rse1, RawScheduleEntry rse2) {
			int tmpResult = rse1.getAirportD().getIATA().compareTo(rse2.getAirportD().getIATA());
			if (tmpResult == 0)
				tmpResult = rse1.getAirportA().getIATA().compareTo(rse2.getAirportA().getIATA());
			if (tmpResult == 0)
				tmpResult = rse1.getTimeD().toLocalTime().compareTo(rse2.getTimeD().toLocalTime());
			if (tmpResult != 0) return tmpResult;
			
			tmpResult = Boolean.compare(rse1.isCodeShare(), rse2.isCodeShare());
			String cs1 = rse1.isCodeShare() ? rse1.getCodeShare() : "";
			String cs2 = rse2.isCodeShare() ? rse2.getCodeShare() : "";
			return (tmpResult == 0) ? cs1.compareTo(cs2) : tmpResult;
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
	 * Merges code share flights, handling cases where there are duplicate entries if a flight is code shared under two different Airlines. <i>This will mutate the input list.</i>
	 * @param entries a Collection of RawScheduleEntry beans
	 * @param f a CodeShareFilter
	 */
	public static void mergeCodeShares(List<RawScheduleEntry> entries, CodeShareFilter f) {
		entries.sort(new CodeShareComparator());
		RawScheduleEntry lastCS = entries.getFirst();
		for (int ofs = 1; ofs < entries.size(); ofs++) {
			RawScheduleEntry rse = entries.get(ofs);
			if (!rse.isCodeShare() || !rse.matches(lastCS) || (FlightNumber.compare(rse, lastCS, false) != 0)) {
				lastCS = rse;
				continue;
			} else if (rse.getCodeShare().equals(lastCS.getCodeShare())) {
				log.info("Duplicate codeshare {} for {}", rse.getCodeShare(), lastCS.getShortCode());
				continue;
			}
			
			// Use the codeshare field as an accumulating buffer
			StringBuilder buf = lastCS.isCodeShare() ? new StringBuilder(lastCS.getCodeShare()) : new StringBuilder();
			buf.append(',').append(rse.getCodeShare());
			lastCS.setCodeShare(buf.toString());
			entries.set(ofs, null); // mark to be removed
			log.info("Merging {}-{} {} into {} now ({})", rse.getAirportD().getIATA(), rse.getAirportA().getIATA(), rse.getCodeShare(), lastCS.getShortCode(), lastCS.getCodeShare());
		}
		
		// Clean out removed entries
		log.info("Filtering codeshares using {}", f);
		int size = entries.size();
		if (entries.removeIf(e -> !f.test(e)))
			log.info("Removed {} duplicate/invalid codeshares", Integer.valueOf(size - entries.size()));
		
		// Remove dupes and format the entries
		for (RawScheduleEntry rse : entries) {
			if (!rse.isCodeShare() || (rse.getCodeShare().indexOf(',') == -1)) continue;
			rse.setRemarks(String.format("Multiple code shares (%s)", rse.getCodeShare()));
			rse.setCodeShare(ScheduleEntry.MULTI_CS);
		}
		
		entries.sort(new LineComparator());
	}
	
	/**
	 * Identifies and merges code share flights based on flight numbers and departure/arrival times. <i>This will mutate the input list.</i>
	 * @param entries a List of RawScheduleEntry beans to merge.
	 * @param f a CodeShareFilter
	 */
	public static void identifyCodeShares(List<RawScheduleEntry> entries, CodeShareFilter f) {
		entries.sort(new FlightTimesComparator());
		RawScheduleEntry lastCS = entries.getFirst();
		for (int ofs = 1; ofs < entries.size(); ofs++) {
			RawScheduleEntry rse = entries.get(ofs);
			if (!possibleCS(rse, lastCS)) {
				lastCS = rse;
				continue;
			}
			
			StringBuilder buf = lastCS.isCodeShare() ? new StringBuilder(lastCS.getCodeShare()).append(',') : new StringBuilder();
			buf.append(rse.getShortCode());
			lastCS.setCodeShare(buf.toString());
			entries.set(ofs, null); // mark to be removed
			log.info("Merged {}-{} {} codeshares {}", lastCS.getAirportD().getIATA(), lastCS.getAirportA().getIATA(), lastCS.getShortCode(), lastCS.getCodeShare());
		}
		
		// Clean out removed entries
		log.info("Filtering codeshares using {}", f);
		int size = entries.size();
		if (entries.removeIf(e -> !f.test(e)))
			log.info("Removed {} duplicate/invalid codeshares", Integer.valueOf(size - entries.size()));
		
		// Remove dupes and format the entries
		for (RawScheduleEntry rse : entries) {
			if (!rse.isCodeShare() || (rse.getCodeShare().indexOf(',') == -1)) continue;
			rse.setRemarks(String.format("Multiple code shares (%s)", rse.getCodeShare()));
			rse.setCodeShare(ScheduleEntry.MULTI_CS);
		}
		
		entries.sort(new LineComparator());
	}
	
	/**
	 * Strips the potential code share flag from schedule entries.
	 * @param entries a Collection of ScheduleEntry beans
	 * @see ScheduleEntry#POTENTIAL_CS
	 */
	public static void stripPotential(Collection<? extends ScheduleEntry> entries) {
		int cnt = 0;
		for (ScheduleEntry se : entries) {
			if (!se.isCodeShare()) continue;
			String cs = se.getCodeShare();
			int pos = cs.indexOf(ScheduleEntry.POTENTIAL_CS);
			if (pos > -1) {
				cs = cs.replace(ScheduleEntry.POTENTIAL_CS, "").replace(",,", ",");
				if (cs.startsWith(",")) cs = cs.substring(1);
				if (cs.length() == 0) cs = null;
				se.setCodeShare(cs);
				cnt++;
			}
		}
		
		log.info("Removed {} potential codeshare flags", Integer.valueOf(cnt));
	}
	
	/**
	 * Updates equipment types based on Airline.
	 * @param rse the RawScheduleEntry
	 * @param eqType the equipment type to match against
	 * @param newEQ the updated equipment type
	 * @param airlineCodes the airline codes to match against, or &quot;*;quot; for all
	 */
	public static void adjustEquipment(RawScheduleEntry rse, String eqType, String newEQ, String... airlineCodes) {
		if (!rse.getEquipmentType().equals(eqType)) return;
		String aCode = rse.getAirline().getCode();
		for (int x = 0; x < airlineCodes.length; x++) {
			String code = airlineCodes[x];
			if ("*".equals(code) || aCode.equals(code)) {
				rse.setEquipmentType(newEQ);
				return;
			}
		}
	}
	
	/*
	 * Helper method to determine if two schedule entries are likely code shares.
	 */
	private static boolean possibleCS(RawScheduleEntry rse1, RawScheduleEntry rse2) {
		if ((rse1 == null) || (rse2 == null) || !rse1.matches(rse2)) return false;
		if (!rse1.getEquipmentType().equals(rse2.getEquipmentType())) return false;
		return rse1.getTimeD().equals(rse2.getTimeD());
	}
}