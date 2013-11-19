// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import org.deltava.beans.GeoLocation;

/**
 * A bean to store GFS winds aloft and tropopause data.
 * @author Luke
 * @version 5.2
 * @since 5.2
 */

public class WindData implements GeoLocation {

	private final double _lat;
	private final double _lng;
	
	private float _jetU;
	private float _jetV;
	private int _jetTMP;

	private int _tropAlt;
	private int _tropTMP;

	/**
	 * Creates the bean.
	 * @param lat the latitude
	 * @param lng the longitude
	 */
	public WindData(double lat, double lng) {
		super();
		_lat = lat;
		_lng = lng;
	}

	public double getLatitude() {
		return _lat;
	}

	public double getLongitude() {
		return _lng;
	}
	
	public int getJetStreamSpeed() {
		double sp = Math.sqrt((_jetU * _jetU) + (_jetV * _jetV));
		return (int) (sp * 1.94384449); 
	}

	public int getJetStreamDirection() {
		return (int) (57.29578 * Math.atan2(_jetU, _jetV) +180); 
	}

	public int getJetStreamTemperature() {
		return _jetTMP;
	}

	public int getTropopauseAltitude() {
		return _tropAlt;
	}

	public int getTropopauseTemperature() {
		return _tropTMP;
	}

	public void setJetStreamU(float u) {
		_jetU = u;
	}

	public void setJetStreamV(float v) {
		_jetV = v;
	}
	
	public void setJetStreamTemperature(int temp) {
		_jetTMP = temp;
	}

	public void setTropopauseAltitude(int alt) {
		_tropAlt = alt;
	}

	public void setTropopauseTemperature(int temp) {
		_tropTMP = temp;
	}
}