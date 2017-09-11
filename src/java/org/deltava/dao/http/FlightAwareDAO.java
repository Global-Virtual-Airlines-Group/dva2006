// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.util.*;
import java.io.IOException;

/**
 * An abstract class to describe FlightAware RESTful Data Access Objects. 
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

abstract class FlightAwareDAO extends DAO {
	
	private String _userID;
	private String _password;

	/**
	 * Sets the User ID to use.
	 * @param usr the user ID
	 */
	public final void setUser(String usr) {
		_userID = usr;
	}
	
	/**
	 * Sets the password to use.
	 * @param password the password
	 */
	public final void setPassword(String password) {
		_password = password;
	}
	
	@Override
	protected void init(String url) throws IOException {
		super.init(url);
		setAuthentication(_userID, _password);
	}
	
	/**
	 * Builds a FlightAware REST URL.
	 * @param method the API method name
	 * @param params a Map of parameters and values
	 * @return the URL to call
	 */
	protected static String buildURL(String method, Map<String, String> params) {
		StringBuilder buf = new StringBuilder("https://flightxml.flightaware.com/json/FlightXML3/");
		buf.append(method);
		buf.append('?');
		for (Iterator<Map.Entry<String, String>> i = params.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<String, String> me = i.next();
			buf.append(me.getKey());
			buf.append('=');
			buf.append(me.getValue());
			if (i.hasNext())
				buf.append('&');
		}
		
		return buf.toString();
	}
}