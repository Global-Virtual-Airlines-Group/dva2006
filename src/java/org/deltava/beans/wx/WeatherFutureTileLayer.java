// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.util.*;
import java.time.Instant;

/**
 * A bean to store future weather tile layers.
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class WeatherFutureTileLayer extends WeatherTileLayer {
	
	private final Map<Instant, SortedSet<Instant>> _sliceDates = new TreeMap<Instant, SortedSet<Instant>>();

	/**
	 * Creates the layer.
	 * @param name the layer name
	 */
	public WeatherFutureTileLayer(String name) {
		super(name);
	}

	/**
	 * Returns the slices available for a particular effective date.
	 * @param effDate the effetive date/time
	 * @return a Collection of Instants
	 */
	public SortedSet<Instant> getSliceDates(Instant effDate) {
		return _sliceDates.getOrDefault(effDate, new TreeSet<Instant>());
	}
	
	/**
	 * Adds an available slice date/time.
	 * @param effDate the effective date/time
	 * @param dt the slice date/time
	 */
	public void addSlice(Instant effDate, Instant dt) {
		SortedSet<Instant> dts = _sliceDates.get(effDate);
		if (dts == null) {
			dts = new TreeSet<Instant>();
			_sliceDates.put(effDate, dts);
		}
			
		dts.add(dt);
	}
}