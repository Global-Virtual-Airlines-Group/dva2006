// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.time.Instant;

import org.gvagroup.tile.TileAddress;

/**
 * A bean to store weather tile overlay layer data.
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class WeatherTileLayer implements Comparable<WeatherTileLayer>, java.io.Serializable {

	private final String _name;
	private final Instant _dt;
	
	private int _nativeZoom;
	private int _maxZoom;

	private TileAddress _topLeft;
	private TileAddress _bottomRight;
	
	/**
	 * Creates the layer bean.
	 * @param name the layer name
	 * @param dt the effective date/time
	 */
	public WeatherTileLayer(String name, Instant dt) {
		super();
		_name = name;
		_dt = dt;
	}
	
	/**
	 * Returns the layer name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the layer effective date.
	 * @return the date/time
	 */
	public Instant getDate() {
		return _dt;
	}
	
	/**
	 * Returns the address of the top-left tile in the layer.
	 * @return the TileAddress
	 */
	public TileAddress getTopLeft() {
		return _topLeft;
	}
	
	/**
	 * Returns the address of the bottom-right tile in the layer.
	 * @return the TileAddress
	 */
	public TileAddress getBottomRight() {
		return _bottomRight;
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
	 * Sets the coordinates of the top-left and bottom-right tiles in this layer.
	 * @param tl the top-left TileAddress
	 * @param br
	 */
	public void setCoordinates(TileAddress tl, TileAddress br) {
		_topLeft = tl;
		_bottomRight = br;
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
		StringBuilder buf = new StringBuilder(_name).append('-');
		buf.append(_dt.toString());
		return buf.toString();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public int compareTo(WeatherTileLayer l2) {
		int tmpResult = _name.compareTo(l2._name);
		return (tmpResult == 0) ? _dt.compareTo(l2._dt) : tmpResult;
	}
}