// Copyright 2008, 2010, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

import org.deltava.beans.*;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store equipment program-specific statistics.
 * @author Luke
 * @version 9.0
 * @since 2.1
 */

public class ProgramMetrics implements Cacheable, Comparable<ProgramMetrics> {

	private final EquipmentType _eq;
	
	private final Map<Rank, Integer> _rankCounts = new TreeMap<Rank, Integer>();
	private final Map<Instant, Integer> _hireCounts = new LinkedHashMap<Instant, Integer>();
	private final Map<PilotStatus, Integer> _statusCounts = new TreeMap<PilotStatus, Integer>();
	
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
		pilots.forEach(p-> addPilot(p));
	}

	/**
	 * Adds a pilot to this bean.
	 * @param p the Pilot to add
	 */
	public void addPilot(Pilot p) {
		if (_eq.getName().equals(p.getEquipmentType())) {
			addRank(p.getRank());
			addHireDate(p.getCreatedOn());
			addStatus(p.getStatus());
			_size++;
		}
	}
	
	/*
	 * Helper method to increment rank count.
	 */
	private void addRank(Rank rank) {
		Integer cnt = _rankCounts.get(rank);
		if (cnt != null) {
			_rankCounts.put(rank, Integer.valueOf(cnt.intValue() + 1));
			_maxRankCount = Math.max(_maxRankCount, cnt.intValue() + 1);
		} else
			_rankCounts.put(rank, Integer.valueOf(1));
	}
	
	/*
	 * Helper method to increment hire date count.
	 */
	private void addHireDate(Instant dt) {
		Instant i = ZonedDateTime.ofInstant(dt, ZoneOffset.UTC).withDayOfMonth(1).toInstant().truncatedTo(ChronoUnit.DAYS);
		Integer cnt = _hireCounts.get(i);
		if (cnt != null) {
			_hireCounts.put(i, Integer.valueOf(cnt.intValue() + 1));
			_maxHireCount = Math.max(_maxHireCount, cnt.intValue() + 1);
		} else
			_hireCounts.put(i, Integer.valueOf(1));
	}
	
	/*
	 * Helper method to increment status count.
	 */
	private void addStatus(PilotStatus status) {
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
	
	public Map<Rank, Integer> getRankCounts() {
		return _rankCounts;
	}
	
	public int getMaxRankCount() {
		return _maxRankCount;
	}
	
	public Map<Instant, Integer> getHireCounts() {
		return _hireCounts;
	}
	
	public int getMaxHireCount() {
		return _maxHireCount;
	}
	
	public Map<PilotStatus, Integer> getStatusCounts() {
		return _statusCounts;
	}
	
	public int getMaxStatusCount() {
		return _maxStatusCount;
	}
	
	@Override
	public int hashCode() {
		return _eq.hashCode();
	}
	
	@Override
	public String toString() {
		return _eq.getName();
	}
	
	@Override
	public Object cacheKey() {
		return _eq.getName();
	}

	/**
	 * Compares two beans by comparing their equipment program beans.
	 */
	@Override
	public int compareTo(ProgramMetrics pm2) {
		return _eq.compareTo(pm2._eq);
	}
}