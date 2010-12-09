// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.deltava.util.StringUtils;

/**
 * An abstract class for Facebook HTTP APIs. 
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public abstract class FacebookDAO extends DAO {

	protected String _appID;
	protected String _appSecret;
	protected String _token;
	
	protected boolean _warnMode;
	
	/**
	 * A helper class to build POST bodies.
	 */
	protected class POSTBuilder extends LinkedHashMap<String, String> {

		/**
		 * Adds a value to the Map provided it is not null or empty.
		 * @param key the key
		 * @param value the value
		 */
		public void addIfPresent(String key, String value) {
			if (!StringUtils.isEmpty(value))
				put(key, value);
		}
		
		/**
		 * Builds a POST body of name/value pairs.
		 */
		public String getBody() {
			StringBuilder dataBuf = new StringBuilder();
			for (Iterator<Map.Entry<String, String>> i = entrySet().iterator(); i.hasNext(); ) {
				Map.Entry<String, String> me = i.next();
				dataBuf.append(me.getKey());
				dataBuf.append('=');
				dataBuf.append(me.getValue());
				if (i.hasNext())
					dataBuf.append('&');
			}
			
			return dataBuf.toString();
		}
	}

	/**
	 * Sets the Facebook application ID to use.
	 * @param id the app ID
	 */
	public void setAppID(String id) {
		_appID = id;
	}
	
	/**
	 * Sets the Facebook application secret to use.
	 * @param key the application secret key
	 */
	public void setSecret(String key) {
		_appSecret = key;
	}
	
	/**
	 * Sets the Graph API access token to use on this request.
	 * @param token the access token
	 */
	public void setToken(String token) {
		_token = token;
	}
	
	/**
	 * Puts this Data Access Object into warning mode, where errors do not throw exceptions
	 * and merely log errors.
	 * @param warnMode TRUE if in warning mode, otherwise FALSE
	 */
	public void setWarnMode(boolean warnMode) {
		_warnMode = warnMode;
	}
}