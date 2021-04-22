// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import org.deltava.beans.GeospaceLocation;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A bean to store VATSIM transceiver data. 
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class RadioPosition implements java.io.Serializable, GeospaceLocation, Comparable<RadioPosition> {
	
	private final String _callsign;
	private final String _freq;
	private final int _seq;
	private GeospaceLocation _pos = new GeoPosition();
	
	/**
	 * Creates the bean.
	 * @param callsign the callsign
	 * @param seq the sequence number
	 * @param freq the frequency
	 */
	public RadioPosition(String callsign, int seq, String freq) {
		super();
		_callsign = callsign.toUpperCase();
		_seq = seq;
		_freq = freq;
	}
	
	/**
	 * Returns the user's callsign.
	 * @return the callsign
	 */
	public String getCallsign() {
		return _callsign;
	}
	
	/**
	 * Returns the radio frequncy.
	 * @return the frequency
	 */
	public String getFrequency() {
		return _freq;
	}
	
	public int getSequence() {
		return _seq;
	}
	
	@Override
	public double getLatitude() {
		return _pos.getLatitude();
	}

	@Override
	public double getLongitude() {
		return _pos.getLongitude();
	}

	@Override
	public int getAltitude() {
		return _pos.getAltitude();
	}
	
	/**
	 * Updates the transceiver location.
	 * @param lat the latitude in degrees
	 * @param lng the longitude in degrees
	 * @param alt the altitude in feet above MSL
	 */
	public void setPosition(double lat, double lng, int alt) {
		_pos = new GeoPosition(lat, lng, alt);
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_callsign);
		buf.append('-').append(_seq);
		return buf.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		return ((o instanceof RadioPosition) && (hashCode() == o.hashCode()));
	}

	@Override
	public int compareTo(RadioPosition rp) {
		int tmpResult = _callsign.compareTo(rp._callsign);
		return (tmpResult == 0) ? Integer.compare(_seq, rp._seq) : tmpResult;
	}
}