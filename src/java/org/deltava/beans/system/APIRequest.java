// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.time.Instant;

/**
 * A bean to store external API usage statistics.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class APIRequest implements java.io.Serializable, Comparable<APIRequest> {
	
	private final Instant _usageTime = Instant.now();
	
	private final String _name;
	private final String _db;
	private final boolean _isAnonymous;
	private final boolean _isBlocked;
	
	/**
	 * Creates the bean.
	 * @param name the API name
	 * @param isAnonymous TRUE if not authenticated, otherwise FALSE
	 */
	public APIRequest(String name, boolean isAnonymous) {
		this (name, null, isAnonymous, false);
	}

	/**
	 * Creates the bean.
	 * @param name the API name
	 * @param db the database to log to
	 * @param isAnonymous TRUE if not authenticated, otherwise FALSE
	 * @param isBlocked TRUE if request was blocked, otherwise FALSE
	 */
	public APIRequest(String name, String db, boolean isAnonymous, boolean isBlocked) {
		super();
		_name = name;
		_db = db;
		_isAnonymous = isAnonymous;
		_isBlocked = isBlocked;
	}
	
	/**
	 * Returns the API name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the time of the API request.
	 * @return the request date/time
	 */
	public Instant getTime() {
		return _usageTime;
	}

	/**
	 * Returns the database name.
	 * @return the database or null if unspecific
	 */
	public String getDB() {
		return _db;
	}
	
	/**
	 * Returns if the request was made by an unauthenticated user.
	 * @return TRUE if anonymous, otherwise FALSE
	 */
	public boolean getIsAnonymous() {
		return _isAnonymous;
	}
	
	/**
	 * Returns if the request was blocked.
	 * @return TRUE if blocked, otherwise FALSE
	 */
	public boolean isBlocked() {
		return _isBlocked;
	}

	@Override
	public int compareTo(APIRequest u2) {
		int tmpResult = _name.compareTo(u2._name);
		return (tmpResult == 0) ? _usageTime.compareTo(u2._usageTime) : tmpResult;
	}
}