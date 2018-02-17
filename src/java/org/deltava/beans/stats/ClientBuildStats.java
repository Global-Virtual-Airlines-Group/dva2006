// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;
import java.time.Instant;

import org.deltava.util.Tuple;

/**
 * A bean to store ACARS client build statistics.
 * @author Luke
 * @version 8.2
 * @since 8.2
 */

public class ClientBuildStats implements Comparable<ClientBuildStats> {

	private final Instant _date;
	
	private final Map<Integer, Tuple<Integer, Double>> _count = new TreeMap<>();

	/**
	 * Creates the bean.
	 * @param dt the start date/time
	 */
	public ClientBuildStats(Instant dt) {
		super();
		_date = dt;
	}
	
	/**
	 * 
	 * @param build
	 * @param legs
	 * @param hours
	 */
	public void addCount(int build, int legs, double hours) {
		_count.put(Integer.valueOf(build), Tuple.create(Integer.valueOf(legs), Double.valueOf(hours)));
	}
	
	public Instant getDate() {
		return _date;
	}
	
	public Collection<Integer> getBuilds() {
		return _count.keySet();
	}
	
	public Tuple<Integer, Double> getCount(int build) {
		return _count.get(Integer.valueOf(build));
	}
	
	@Override
	public int hashCode() {
		return _date.hashCode();
	}

	@Override
	public int compareTo(ClientBuildStats cb) {
		return _date.compareTo(cb._date);
	}
}