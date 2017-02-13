// Copyright 2010, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

/**
 * An abstract class for Facebook HTTP APIs. 
 * @author Luke
 * @version 7.2
 * @since 3.4
 */

public abstract class FacebookDAO extends DAO {

	protected String _appID;
	protected String _appSecret;
	protected String _token;
	
	protected boolean _warnMode;
	
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