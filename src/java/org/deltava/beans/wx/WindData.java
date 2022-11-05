// Copyright 2013, 2014, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import org.deltava.beans.GeospaceLocation;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store GFS winds aloft and tropopause data.
 * @author Luke
 * @version 10.3
 * @since 5.2
 */

public class WindData implements GeospaceLocation, Cacheable {

	private final double _lat;
	private final double _lng;
	private final PressureLevel _pl;
	
	private int _alt;
	private int _tmp;
	
	private float _jetU;
	private float _jetV;

	/**
	 * Creates the bean.
	 * @param lvl the PressureLevel
	 * @param lat the latitude
	 * @param lng the longitude
	 */
	public WindData(PressureLevel lvl, double lat, double lng) {
		super();
		_pl = lvl;
		_lat = lat;
		_lng = lng;
	}

	@Override
	public double getLatitude() {
		return _lat;
	}

	@Override
	public double getLongitude() {
		return _lng;
	}
	
	@Override
	public int getAltitude() {
		return _alt;
	}
	
	/**
	 * Returns the pressure level for this data.
	 * @return a PressureLevel
	 */
	public PressureLevel getLevel() {
		return _pl;
	}
	
	public int getTemperature() {
		return _tmp;
	}
	
	/**
	 * Returns the jet stream wind speed.
	 * @return the speed in knots
	 */
	public int getJetStreamSpeed() {
		double sp = Math.sqrt((_jetU * _jetU) + (_jetV * _jetV));
		return (int) (sp * 1.94384449); 
	}

	/**
	 * Returns the jet stream wind direction.
	 * @return the direction in degrees
	 */
	public int getJetStreamDirection() {
		return (int)((270-Math.toDegrees(Math.atan2(_jetU, _jetV))) % 360);
	}

	/**
	 * Sets the U wind component speed.
	 * @param u the speed in m/s
	 */
	public void setJetStreamU(float u) {
		_jetU = u;
	}

	/**
	 * Sets the V wind component speed.
	 * @param v the speed in m/s
	 */
	public void setJetStreamV(float v) {
		_jetV = v;
	}
	
	public void setAltitude(int alt) {
		_alt = alt;
	}
	
	public void setTemperature(int tmp) {
		_tmp = tmp;
	}
	
	@Override
	public Object cacheKey() {
		StringBuilder buf = new StringBuilder(_pl.toString());
		buf.append('-');
		buf.append(_lat);
		buf.append('-');
		buf.append(_lng);
		return buf.toString();
	}
	
	@Override
	public int hashCode() {
		return cacheKey().hashCode();
	}
}