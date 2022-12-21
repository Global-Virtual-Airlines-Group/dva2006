// Copyright 2017, 2018, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

/**
 * A bean to store ordered statistics entries that contain a key value plus hours/legs/distance. 
 * @author Luke
 * @version 10.3
 * @since 8.3
 * @param <K> The sort key
 */

abstract class LegHoursStatsEntry<K extends Comparable<K>> implements java.io.Serializable {

	private static final LegHoursStats ZERO = new LegHoursStats(0, 0, 0);
	
	private final SortedMap<K, LegHoursStats> _legs = new TreeMap<K, LegHoursStats>();

	/**
	 * Returns tthe available keys.
	 * @return a Collection of keys
	 */
	public Collection<K> getKeys() {
		return _legs.keySet();
	}
	
	/**
	 * Returns the number of flight legs for a particular key.
	 * @param key the key value
	 * @return the number of legs
	 */
	public int getLegs(K key) {
		return _legs.getOrDefault(key, ZERO).getLegs();
	}
	
	/**
	 * Returns the flight distance for a particular key.
	 * @param key the key value
	 * @return the distance in miles 
	 */
	public int getDistance(K key) {
		return _legs.getOrDefault(key, ZERO).getDistance();
	}
	
	/**
	 * Returns the number of flight hours for a particular key.
	 * @param key the key value
	 * @return the number of hours
	 */
	public double getHours(K key) {
		return _legs.getOrDefault(key, ZERO).getHours();
	}
	
	/**
	 * Returns the maximum key value in this bean.
	 * @return the highest key value
	 */
	protected K getMaxKey() {
		return _legs.lastKey();
	}
	
	/**
	 * Adds a statistics entry.
	 * @param key the key
	 * @param legs the number of flight legs
	 * @param distance the flight distance in miles
	 * @param hours the number of flight hours
	 */
	protected void set(K key, int legs, int distance, double hours) {
		_legs.put(key, new LegHoursStats(legs, distance, hours));
	}
}