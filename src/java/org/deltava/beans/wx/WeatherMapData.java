// Copyright 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.util.*;
import java.time.Instant;

import org.deltava.util.cache.ExpiringCacheable;

/**
 * A bean to store consolidated front/cyclone data.
 * @author Luke
 * @version 5.2
 * @since 5.2
 */

public class WeatherMapData implements ExpiringCacheable, Comparable<WeatherMapData> {

	private final Instant _dt;
	private final Instant _expiryDate;
	
	private final List<WeatherLine> _fronts = new ArrayList<WeatherLine>();
	private final List<Cyclone> _cyclones = new ArrayList<Cyclone>();
	
	/**
	 * Creates the bean.
	 * @param dt the effective date/time in UTC
	 */
	public WeatherMapData(Instant dt) {
		super();
		_dt = dt;
		
		// Calculate expiry - typically three hours from generation or 2 minutes
		long expDT = Math.max(_dt.toEpochMilli() + (3600_000 * 3), System.currentTimeMillis() + 120_000);
		_expiryDate = Instant.ofEpochMilli(expDT);
	}
	
	/**
	 * Adds a front/trough.
	 * @param wl the WeatherLine
	 */
	public void add(WeatherLine wl) {
		_fronts.add(wl);
	}
	
	/**
	 * Adds a cyclone.
	 * @param c the Cyclone
	 */
	public void add(Cyclone c) {
		_cyclones.add(c);
	}
	
	public Instant getDate() {
		return _dt;
	}
	
	public Collection<Cyclone> getCyclones() {
		return Collections.unmodifiableList(_cyclones);
	}
	
	public Collection<WeatherLine> getFronts() {
		return Collections.unmodifiableList(_fronts);
	}

	@Override
	public Object cacheKey() {
		return "SFCMAP";
	}

	@Override
	public Instant getExpiryDate() {
		return _expiryDate;
	}

	/**
	 * Compres two sets of weather map data by comparing their effective dates.
	 */
	@Override
	public int compareTo(WeatherMapData wmd) {
		return _dt.compareTo(wmd._dt);
	}
}