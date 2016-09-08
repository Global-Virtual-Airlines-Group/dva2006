// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.log;

import java.util.*;

import com.newrelic.api.agent.*;

/**
 * A request class for NewRelic non-web transactions. 
 * @author Luke
 * @version 7.2
 * @since 7.2
 */

@SuppressWarnings("deprecation")
public class SyntheticRequest extends ExtendedRequest {
	
	private final String _name;
	private final String _userID;

	/**
	 * Creates the request.
	 * @param name the request name
	 * @param userID the user ID
	 */
	public SyntheticRequest(String name, String userID) {
		super();
		_name = name;
		_userID = userID;
	}

	/* (non-Javadoc)
	 * @see com.newrelic.api.agent.Request#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCookieValue(String name) {
		return null;
	}

	@Override
	public Enumeration<?> getParameterNames() {
		return Collections.emptyEnumeration();
	}

	@Override
	public String[] getParameterValues(String arg0) {
		return new String[0];
	}

	@Override
	public String getRemoteUser() {
		return _userID;
	}
	
	@Override
	public String getRequestURI() {
		return _name;
	}

	@Override
	public String getHeader(String arg0) {
		return null;
	}

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.HTTP;
	}

	@Override
	public String getMethod() {
		return "GET";
	}
}