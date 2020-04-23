// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;
import java.util.stream.*;

import org.deltava.beans.Helper;

import org.deltava.comparators.ScheduleEntryComparator;

/**
 * A utility class to assign leg numbers to schedule entries. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

@Helper(ScheduleEntry.class)
public class ScheduleLegHelper {
	
	private static final Comparator<ScheduleEntry> SRT = new ScheduleEntryComparator(ScheduleEntryComparator.ATIME);

	// static class
	private ScheduleLegHelper() {
		super();
	}

	public static <T extends ScheduleEntry> Collection<T> calculateLegs(Collection<T> entries) {
		
		// Divide into buckets based on flight number
		Map<String, List<T>> fMap = new HashMap<String, List<T>>();
		entries.stream().forEach(se -> addEntry(fMap, se.getShortCode(), se));
		
		// Sort each bucket based on arrival time
		return fMap.entrySet().stream().map(Map.Entry::getValue).flatMap(ScheduleLegHelper::setLegs).collect(Collectors.toList());
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