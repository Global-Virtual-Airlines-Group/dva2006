// Copyright 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.deltava.util.Tuple;

/**
 * A bean to store ordered statistics entries that contain a key value plus hours/legs. 
 * @author Luke
 * @version 8.6
 * @param <K> The sort key
 * @since 8.3
 */

abstract class LegHoursStatsEntry<K extends Comparable<K>> implements java.io.Serializable {

	private static final Tuple<Integer, Double> ZERO = Tuple.create(Integer.valueOf(0), Double.valueOf(0));
	
	private final Instant _dt;
	private final SortedMap<K, Tuple<Integer, Double>> _legs = new TreeMap<K, Tuple<Integer, Double>>();

	/**
	 * Creates the bean.
	 * @param dt the date/time
	 */
	public LegHoursStatsEntry(Instant dt) {
		super();
		_dt = dt.truncatedTo(ChronoUnit.DAYS);
	}
	
	/**
	 * Returns the date.
	 * @return the date
	 */
	public Instant getDate() {
		return _dt;
	}
	
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
		return _legs.getOrDefault(key, ZERO).getLeft().intValue();
	}
	
	/**
	 * Returns the number of flight hours for a particular key.
	 * @param key the key value
	 * @return the number of hours
	 */
	public double getHours(K key) {
		return _legs.getOrDefault(key, ZERO).getRight().doubleValue();
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
	 * @param legs the number of legs
	 * @param hours the number of hours
	 */
	protected void set(K key, int legs, double hours) {
		_legs.put(key, Tuple.create(Integer.valueOf(legs), Double.valueOf(hours)));
	}
	
	@Override
	public int hashCode() {
		return _dt.hashCode();
	}
}