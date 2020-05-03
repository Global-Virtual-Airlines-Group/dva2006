// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.time.Instant;

/**
 * A bean to store external API usage statistics.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class APIRequest implements Comparable<APIRequest> {
	
	private final Instant _usageTime = Instant.now();
	
	private final String _name;
	private final boolean _isAnonymous; 

	/**
	 * Creates the bean.
	 * @param name the API name
	 * @param isAnonymous TRUE if not authenticated, otherwise FALSE
	 */
	public APIRequest(String name, boolean isAnonymous) {
		super();
		_name = name;
		_isAnonymous = isAnonymous;
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
	 * Returns if the request was made by an unauthenticated user.
	 * @return TRUE if anonymous, otherwise FALSE
	 */
	public boolean getIsAnonymous() {
		return _isAnonymous;
	}

	@Override
	public int compareTo(APIRequest u2) {
		int tmpResult = _name.compareTo(u2._name);
		return (tmpResult == 0) ? _usageTime.compareTo(u2._usageTime) : tmpResult;
	}
}