// Copyright 2017, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.util.*;
import java.time.Instant;

/**
 * A bean to store future weather tile layers.
 * @author Luke
 * @version 11.6
 * @since 8.0
 */

@Deprecated
public class WeatherFutureTileLayer extends WeatherTileLayer {
	
	private final Map<Instant, SortedSet<TileDate>> _sliceDates = new TreeMap<Instant, SortedSet<TileDate>>();

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
	 * @return a Collection of TileDates
	 */
	public SortedSet<TileDate> getSliceDates(Instant effDate) {
		return _sliceDates.getOrDefault(effDate, new TreeSet<TileDate>());
	}
	
	/**
	 * Adds an available slice date/time.
	 * @param effDate the effective date/time
	 * @param td the slice TileDate
	 */
	public void addSlice(Instant effDate, TileDate td) {
		SortedSet<TileDate> dts = _sliceDates.get(effDate);
		if (dts == null) {
			dts = new TreeSet<TileDate>();
			_sliceDates.put(effDate, dts);
		}
			
		dts.add(td);
	}
}