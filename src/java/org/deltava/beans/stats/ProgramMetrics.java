// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.CalendarUtils;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store equipment program-specific statistics.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class ProgramMetrics implements Cacheable, Comparable<ProgramMetrics> {

	private EquipmentType _eq;
	
	private final Map<String, Integer> _rankCounts = new LinkedHashMap<String, Integer>();
	private final Map<Date, Integer> _hireCounts = new LinkedHashMap<Date, Integer>();
	private final Map<String, Integer> _statusCounts = new TreeMap<String, Integer>();
	
	private int _size;
	private int _maxRankCount = 1;
	private int _maxHireCount = 1;
	private int _maxStatusCount = 1;
	
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
		for (Pilot p : pilots) 
			addPilot(p);
	}

	/**
	 * Adds a pilot to this bean.
	 * @param p the Pilot to add
	 */
	public void addPilot(Pilot p) {
		if (_eq.getName().equals(p.getEquipmentType())) {
			addRank(p.getRank());
			addHireDate(p.getCreatedOn());
			addStatus(p.getStatusName());
			_size++;
		}
	}
	
	/**
	 * Helper method to increment rank count.
	 */
	private void addRank(String rank) {
		Integer cnt = _rankCounts.get(rank);
		if (cnt != null) {
			_rankCounts.put(rank, Integer.valueOf(cnt.intValue() + 1));
			_maxRankCount = Math.max(_maxRankCount, cnt.intValue() + 1);
		} else
			_rankCounts.put(rank, Integer.valueOf(1));
	}
	
	/**
	 * Helper method to increment hire date count.
	 */
	private void addHireDate(Date dt) {
		Calendar cld = CalendarUtils.getInstance(dt, true);
		cld.set(Calendar.DAY_OF_MONTH, 1);
		Integer cnt = _hireCounts.get(cld.getTime());
		if (cnt != null) {
			_hireCounts.put(cld.getTime(), Integer.valueOf(cnt.intValue() + 1));
			_maxHireCount = Math.max(_maxHireCount, cnt.intValue() + 1);
		} else
			_hireCounts.put(cld.getTime(), Integer.valueOf(1));
	}
	
	/**
	 * Helper method to increment status count.
	 */
	private void addStatus(String status) {
		Integer cnt = _statusCounts.get(status);
		if (cnt != null) {
			_statusCounts.put(status, Integer.valueOf(cnt.intValue() + 1));
			_maxStatusCount = Math.max(_maxStatusCount, cnt.intValue() + 1);
		} else
			_statusCounts.put(status, Integer.valueOf(1));
	}
	
	/**
	 * Returns the number of Pilots in the program.
	 * @return the number of Pilots
	 */
	public int getSize() {
		return _size;
	}
	
	public Map<String, Integer> getRankCounts() {
		return _rankCounts;
	}
	
	public int getMaxRankCount() {
		return _maxRankCount;
	}
	
	public Map<Date, Integer> getHireCounts() {
		return _hireCounts;
	}
	
	public int getMaxHireCount() {
		return _maxHireCount;
	}
	
	public Map<String, Integer> getStatusCounts() {
		return _statusCounts;
	}
	
	public int getMaxStatusCount() {
		return _maxStatusCount;
	}
	
	public int hashCode() {
		return _eq.hashCode();
	}
	
	public String toString() {
		return _eq.getName();
	}
	
	public Object cacheKey() {
		return _eq.getName();
	}

	/**
	 * Compares two beans by comparing their equipment program beans.
	 */
	public int compareTo(ProgramMetrics pm2) {
		return _eq.compareTo(pm2._eq);
	}
}