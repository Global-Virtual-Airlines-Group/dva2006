// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.util.Date;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.navdata.AirportLocation;

import org.deltava.util.cache.ExpiringCacheable;

/**
 * A bean to store weather data for a particular location.
 * @author Luke
 * @version 2.6
 * @since 2.2
 */

public abstract class WeatherDataBean implements MarkerMapEntry, ExpiringCacheable, Comparable<WeatherDataBean> {

	private AirportLocation _pos;
	private Date _createdOn;
	private Date _obsDate;

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
			WeatherDataBean wx = (WeatherDataBean) c.newInstance();
			return wx;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Initializes the bean.
	 * @throws NullPointerException if code is null
	 */
	public WeatherDataBean() {
		super();
		_createdOn = new Date();
	}

	/**
	 * Returns the effective date of this weather data.
	 * @return the effective date/time
	 */
	public Date getDate() {
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
	public Date getCreatedOn() {
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
	public double getLatitude() {
		return _pos.getLatitude();
	}
	
	/**
	 * Returns the longitude of this observation.
	 */
	public double getLongitude() {
		return _pos.getLongitude();
	}
	
	/**
	 * Returns the data type.
	 * @return the data type
	 */
	public abstract String getType();
	
	/**
	 * Sets the effective date of this weather data.
	 * @param dt the effective date/time
	 */
	public void setDate(Date dt) {
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
	
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder();
		if (_pos != null) {
			buf.append(_pos.getInfoBox());
			buf.append("<br /><br />");
		}
		
		// Append the weather data
		buf.append("<span class=\"mapInfoBox\"><b>");
		buf.append(getType());
		buf.append(" Data</b>:<br />");
		buf.append(getData());
		buf.append("</span>");
		return buf.toString();
	}
	
	public Object cacheKey() {
		StringBuilder buf = new StringBuilder(getType());
		buf.append('$');
		buf.append(_pos.getCode());
		return buf.toString();
	}

	/**
	 * Compares two beans by comparing their effectve dates, creation dates and data types.
	 */
	public int compareTo(WeatherDataBean o) {
		int tmpResult = _obsDate.compareTo(o.getDate());
		if (tmpResult == 0)
			tmpResult = _createdOn.compareTo(o.getCreatedOn());
		return (tmpResult == 0) ? getType().compareTo(o.getType()) : tmpResult;
	}
	
	public boolean equals(Object o) {
		return (o instanceof WeatherDataBean) ? (compareTo((WeatherDataBean) o) == 0) : false;
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder(getType());
		buf.append(' ');
		buf.append(getCode());
		buf.append(' ');
		buf.append(getData());
		return buf.toString();
	}
}