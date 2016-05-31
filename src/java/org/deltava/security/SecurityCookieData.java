// Copyright 2005, 2006, 2011, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.time.Instant;

/**
 * A bean containing data stored in the security cookie.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class SecurityCookieData implements java.io.Serializable {
    
    private transient static final long DEFAULT_EXPIRY = 3600 * 8;
    
 	private final String _userID;
	private String _remoteAddr;
	private Instant _loginDate;
	private Instant _expiryDate;
	
	/**
	 * Creates security cookie data for a given user ID.
	 * @param userID the user ID
	 */
    public SecurityCookieData(String userID) {
        super();
        _userID = userID;
        setExpiryDate(Instant.now().plusSeconds(DEFAULT_EXPIRY));
    }
    
    /**
     * Returns the expiration date of the cookie.
     * @return the expiration date
     */
    public Instant getExpiryDate() {
        return _expiryDate;
    }
    
    /**
     * Returns the login date.
     * @return the login date
     */
    public Instant getLoginDate() {
    	return _loginDate;
    }
    
 	/**
 	 * Retrieves the user ID from this security cookie.
 	 * @return the Directory Name of the user
 	 */
 	public String getUserID() {
 		return _userID;
 	}
 	
	 /**
	  * Returns the remote address of the user.
	 * @return the IP address
	 */
	public String getRemoteAddr() {
		return _remoteAddr;
	}
	
	/**
	 * Checks if the security cookie has expired.
	 * @return TRUE if the cookie has expired, otherwise FALSE
	 */
	public boolean isExpired() {
		return (System.currentTimeMillis() > _expiryDate.toEpochMilli());
	}
	
	/**
	 * Sets the expiry date of the security cookie.
	 * @param dt the expiration date
	 */
	public void setExpiryDate(Instant dt) {
	    _expiryDate = dt;
	}
	
	/**
	 * Sets the login date.
	 * @param dt the login date
	 */
	public void setLoginDate(Instant dt) {
		_loginDate = dt;
	}
    
 	/**
 	 * Updates the user's IP address.
 	 * @param remoteAddr the IP address
 	 */
 	public void setRemoteAddr(String remoteAddr) {
 		_remoteAddr = remoteAddr;
 	}
 	
 	@Override
 	public int hashCode() {
 		return _userID.hashCode();
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder buf = new StringBuilder(_userID);
 		buf.append('@');
 		buf.append(_remoteAddr);
 		return buf.toString();
 	}
}