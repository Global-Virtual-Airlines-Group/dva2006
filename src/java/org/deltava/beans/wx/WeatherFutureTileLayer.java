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
	
	private final SortedSet<Instant> _sliceDates = new TreeSet<Instant>();

	/**
	 * Creates the layer.
	 * @param name the layer name
	 * @param dt the effective date/time
	 */
	public WeatherFutureTileLayer(String name, Instant dt) {
		super(name, dt);
	}

	/**
	 * Returns the slices available.
	 * @return a Collection of Instants
	 */
	public SortedSet<Instant> getSliceDates() {
		return _sliceDates;
	}
	
	/**
	 * Adds an available slice date/time to the layer.
	 * @param dt the slice date/time
	 */
	public void addSlice(Instant dt) {
		_sliceDates.add(dt);
	}
}