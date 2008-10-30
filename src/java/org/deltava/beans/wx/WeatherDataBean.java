// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.util.Date;

import org.deltava.util.cache.ExpiringCacheable;

/**
 * A bean to store weather data for a particular location.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public abstract class WeatherDataBean implements ExpiringCacheable, Comparable<WeatherDataBean> {
	
	private Date _createdOn;
	private Date _obsDate;
	private String _obsStnCode;
	
	private String _wxData;

	/**
	 * Initializes the bean.
	 * @param code the observation station code
	 * @throws NullPointerException if code is null
	 */
	public WeatherDataBean(String code) {
		super();
		_obsStnCode = code.toUpperCase();
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
		return _obsStnCode;
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
	 * Updates the weather data.
	 * @param data the data
	 */
	public void setData(String data) {
		_wxData = data;
	}
	
	public Object cacheKey() {
		StringBuilder buf = new StringBuilder(getType());
		buf.append('$');
		buf.append(_obsStnCode);
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