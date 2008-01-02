// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.CalendarUtils;

/**
 * A bean to store equipment program-specific statistics.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class ProgramMetrics implements Comparable<ProgramMetrics> {

	private EquipmentType _eq;
	
	private final Collection<Pilot> _pilots = new LinkedHashSet<Pilot>();
	private final Map<String, Integer> _rankCounts = new LinkedHashMap<String, Integer>();
	private final Map<Date, Integer> _hireCounts = new LinkedHashMap<Date, Integer>();
	
	/**
	 * Initializes the bean.
	 * @param eq the EquipmentType
	 */
	public ProgramMetrics(EquipmentType eq) {
		super();
		_eq = eq;
	}
	
	/**
	 * Adds pilots to this bean.
	 * @param pilots the Pilots in the equipment program
	 */
	public void addPilots(Collection<Pilot> pilots) {
		for (Pilot p : pilots) {
			if (_eq.getName().equals(p.getEquipmentType())) {
				_pilots.add(p);
				addRank(p.getRank());
				addHireDate(p.getCreatedOn());
			}
		}
	}
	
	/**
	 * Helper method to increment rank count.
	 */
	private void addRank(String rank) {
		Integer cnt = _rankCounts.get(rank);
		if (cnt != null)
			_rankCounts.put(rank, Integer.valueOf(cnt.intValue() + 1));
		else
			_rankCounts.put(rank, Integer.valueOf(1));
	}
	
	/**
	 * Helper method to increment hire date count.
	 */
	private void addHireDate(Date dt) {
		Calendar cld = CalendarUtils.getInstance(dt, true);
		cld.set(Calendar.DAY_OF_MONTH, 1);
		Integer cnt = _hireCounts.get(cld.getTime());
		if (cnt != null)
			_hireCounts.put(cld.getTime(), Integer.valueOf(cnt.intValue() + 1));
		else
			_hireCounts.put(cld.getTime(), Integer.valueOf(1));
	}
	
	/**
	 * Returns all the Pilots in this program.
	 * @return a Collection of Pilots
	 */
	public Collection<Pilot> getPilots() {
		return new ArrayList<Pilot>(_pilots);
	}
	
	public int hashCode() {
		return _eq.hashCode();
	}
	
	public String toString() {
		return _eq.getName();
	}

	/**
	 * Compares two beans by comparing their equipment program beans.
	 */
	public int compareTo(ProgramMetrics pm2) {
		return _eq.compareTo(pm2._eq);
	}
}