// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.Instant;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store external API usage statistics.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class APIUsage implements Cacheable, Comparable<APIUsage> {

	private final Instant _date;
	private final String _name;
	
	private int _total;
	private int _anonymous;
	private int _blocked;
	
	/**
	 * Creates the bean.
	 * @param dt the date/time
	 * @param name the API method name
	 */
	public APIUsage(Instant dt, String name) {
		super();
		_date = dt;
		_name = name;
	}

	/**
	 * Returns the API/method name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the statistics date.
	 * @return the date/time
	 */
	public Instant getDate() {
		return _date;
	}
	
	/**
	 * Returns the total number of API invocations.
	 * @return the number of invocations
	 */
	public int getTotal() {
		return _total;
	}
	
	/**
	 * Returns the number of anonymous API invocations.
	 * @return the number of invocations
	 */
	public int getAnonymous() {
		return _anonymous;
	}
	
	/**
	 * Returns the number of blocked API invocations.
	 * @return the number of blocked invocations
	 */
	public int getBlocked() {
		return _blocked;
	}
	
	/**
	 * Updates the total number of API invocations.
	 * @param cnt the number of invocations
	 */
	public void setTotal(int cnt) {
		_total = cnt;
	}
	
	/**
	 * Updates the number of anonymous API invocations.
	 * @param cnt the number of invocations
	 */
	public void setAnonymous(int cnt) {
		_anonymous = cnt;
	}
	
	/**
	 * Updates the number of blocked API invocations.
	 * @param cnt the number of invocations
	 */
	public void setBlocked(int cnt) {
		_blocked = cnt;
	}

	@Override
	public int compareTo(APIUsage u2) {
		int tmpResult = _date.compareTo(u2._date);
		return (tmpResult == 0) ? _name.compareTo(u2._name) : tmpResult;
	}

	@Override
	public Object cacheKey() {
		StringBuilder buf = new StringBuilder(_name);
		buf.append('-').append(_date.toEpochMilli());
		return buf.toString();
	}
}