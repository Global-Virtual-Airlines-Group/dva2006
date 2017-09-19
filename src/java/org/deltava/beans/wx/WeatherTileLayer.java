// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.util.*;
import java.time.Instant;

/**
 * A bean to store weather tile overlay layer data.
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class WeatherTileLayer implements Comparable<WeatherTileLayer>, java.io.Serializable {

	private final String _name;
	private final Collection<Instant> _dates = new TreeSet<Instant>(Collections.reverseOrder());
	
	private int _nativeZoom;
	private int _maxZoom;

	/**
	 * Creates the layer bean.
	 * @param name the layer name
	 */
	public WeatherTileLayer(String name) {
		super();
		_name = name;
	}
	
	/**
	 * Returns the layer name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the layer effective dates.
	 * @return a Collection of Instants
	 */
	public Collection<Instant> getDates() {
		return _dates;
	}
	
	/**
	 * Returns the layer's native zoom level.
	 * @return the native zoom level
	 */
	public int getNativeZoom() {
		return _nativeZoom;
	}
	
	/**
	 * Returns the layer's maximum zoom level.
	 * @return the maximum zoom level
	 */
	public int getMaxZoom() {
		return _maxZoom;
	}
	
	/**
	 * Adds an effective date to this layer.
	 * @param dt a date/time
	 */
	public void addDate(Instant dt) {
		_dates.add(dt);
	}
	
	/**
	 * Sets the native and maximum zoom levels for this layer.
	 * @param nativeZoom the native zoom level
	 * @param maxZoom the maximum zoom level
	 */
	public void setZoom(int nativeZoom, int maxZoom) {
		_nativeZoom = nativeZoom;
		_maxZoom = Math.max(nativeZoom, maxZoom);
	}
	
	@Override
	public String toString() {
		return _name;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public int compareTo(WeatherTileLayer l2) {
		return _name.compareTo(l2._name);
	}
}