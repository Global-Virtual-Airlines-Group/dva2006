// Copyright 2008, 2009, 2010, 2012, 2014, 2015, 2106, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.navdata.AirportLocation;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store weather data for a particular location.
 * @author Luke
 * @version 8.1
 * @since 2.2
 */

public abstract class WeatherDataBean implements MarkerMapEntry, Cacheable, Comparable<WeatherDataBean> {

	private AirportLocation _pos;
	private Instant _createdOn = Instant.now();
	private Instant _obsDate;
	private String _wxData;
	
	/**
	 * An enumeration to store valid weather data types.
	 */
	public enum Type {
		METAR, TAF;
	}

	/**
	 * Creates an arbitrary weather bean type.
	 * @param t the bean type
	 * @return a WeatherDataBean, or null if unknown
	 */
	public static WeatherDataBean create(Type t) {
		try {
			Class<?> c = Class.forName(WeatherDataBean.class.getPackage().getName() + "." + t.toString());
			return (WeatherDataBean) c.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Returns the effective date of this weather data.
	 * @return the effective date/time
	 */
	public Instant getDate() {
		return _obsDate;
	}
	
	/**
	 * Returns the observation station code.
	 * @return the code
	 */
	public String getCode() {
		return (_pos == null) ? null : _pos.getCode();
	}
	
	/**
	 * Returns the observation station name.
	 * @return the name
	 */
	public String getName() {
		return _pos.getName();
	}
	
	/**
	 * Returns the creation date of this weather object.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the weather data.
	 * @return the data
	 */
	public String getData() {
		return _wxData;
	}
	
	/**
	 * Returns the latitude of this observation.
	 */
	@Override
	public double getLatitude() {
		return _pos.getLatitude();
	}
	
	/**
	 * Returns the longitude of this observation.
	 */
	@Override
	public double getLongitude() {
		return _pos.getLongitude();
	}
	
	/**
	 * Returns the data type.
	 * @return the data type
	 */
	public abstract Type getType();
	
	/**
	 * Sets the effective date of this weather data.
	 * @param dt the effective date/time
	 */
	public void setDate(Instant dt) {
		_obsDate = dt;
	}
	
	/**
	 * Sets the geographic location of this Observation station.
	 * @param al an AirportLocation bean
	 */
	public void setAirport(AirportLocation al) {
		_pos = al;
	}
	
	/**
	 * Sets the geographic location of this Observation station.
	 * @param a an Airport bean
	 */
	public void setAirport(Airport a) {
		if (a != null)
			_pos = new AirportLocation(a);
	}
	
	/**
	 * Updates the weather data.
	 * @param data the data
	 */
	public void setData(String data) {
		_wxData = data;
	}
	
	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder();
		if (_pos != null) {
			buf.append(_pos.getInfoBox());
			buf.append("<br /><br />");
		}
		
		// Append the weather data
		buf.append("<div class=\"mapInfoBox wx\"><span class=\"bld\">");
		buf.append(getType());
		buf.append(" Data</span>:<br />");
		buf.append(getData());
		buf.append("</div>");
		return buf.toString();
	}
	
	@Override
	public Object cacheKey() {
		return _pos.getCode();
	}

	/**
	 * Compares two beans by comparing their effectve dates, creation dates and data types.
	 */
	@Override
	public int compareTo(WeatherDataBean o) {
		int tmpResult = _obsDate.compareTo(o.getDate());
		if (tmpResult == 0)
			tmpResult = _createdOn.compareTo(o.getCreatedOn());
		return (tmpResult == 0) ? getType().compareTo(o.getType()) : tmpResult;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof WeatherDataBean) ? (compareTo((WeatherDataBean) o) == 0) : false;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(getType().toString());
		buf.append(' ');
		buf.append(getCode());
		buf.append(' ');
		buf.append(getData());
		return buf.toString();
	}
}