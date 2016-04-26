// Copyright 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A collection to store a gridded result from a GRIB file, tracking the corners of the grid.
 * @author Luke
 * @version 7.0
 * @since 5.2
 */

public class GRIBResult<T extends GeoLocation> extends ArrayList<T> implements GeoLocation {

	private final int _h;
	private final int _w;
	
	private final float _dLat;
	private final float _dLng;
	
	private double _maxLat;
	private double _maxLng;
	private double _minLat;
	private double _minLng;
	
	/**
	 * Initializes the collection.
	 */
	public GRIBResult(int w, int h, float dLat, float dLng) {
		super(8192);
		_h = h; _w = w;
		_dLat = dLat; _dLng = dLng;
	}
	
	public void setStart(float lat, float lng) {
		_maxLat = lat;
		_minLng = lng;
		_minLat = Math.max(-90, lat - (_h * _dLat));
		_maxLng = Math.min(180, lng + (_w * _dLng));
	}

	@Override
	public double getLatitude() {
		return (_maxLat - _minLat) /2;
	}
	
	@Override
	public double getLongitude() {
		return (_maxLng - _minLng) / 2;
	}
	
	public GeoLocation getNW() {
		return new GeoPosition(_maxLat, _minLng);
	}
	
	public GeoLocation getSE() {
		return new GeoPosition(_minLat, _maxLng);
	}
	
	public T getResult(GeoLocation loc) {
		if ((loc.getLatitude() > _maxLat) || (loc.getLatitude() < _minLat) || (loc.getLongitude() < _minLng)
				|| (loc.getLongitude() > _maxLng)) return null;
		
		// Calculate x and y based on start
		int x = Math.min((int) Math.round((loc.getLongitude() - _minLng) / _dLng), _w-1);
		int y = (int) Math.round((_maxLat - loc.getLatitude()) / _dLat);
		
		// Convert to index offset
		int ofs = (y * _w) + x;
		return get(ofs);
	}
}