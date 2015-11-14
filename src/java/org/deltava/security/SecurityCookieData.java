// Copyright 2005, 2006, 2011, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

/**
 * A bean containing data stored in the security cookie.
 * @author Luke
 * @version 6.3
 * @since 1.0
 */

public class SecurityCookieData implements java.io.Serializable {
    
    private transient static final long DEFAULT_EXPIRY = 360 * 60000; // 6 hours
    
 	private final String _userID;
	private String _remoteAddr;
	private long _loginDate;
	private long _expiryDate;
	
	/**
	 * Creates security cookie data for a given user ID.
	 * @param userID the user ID
	 */
    public SecurityCookieData(String userID) {
        super();
        _userID = userID;
        setExpiryDate(System.currentTimeMillis() + DEFAULT_EXPIRY);
    }
    
    /**
     * Returns the expiration date of the cookie.
     * @return the expiration date as a 64-bit Unix timestamp
     */
    public long getExpiryDate() {
        return _expiryDate;
    }
    
    /**
     * Returns the login date.
     * @return the login date as a 64-bit Unix timestamp
     */
    public long getLoginDate() {
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
		return (System.currentTimeMillis() > _expiryDate);
	}
	
	/**
	 * Sets the expiry date of the security cookie.
	 * @param dt the expiration date as a 64-bit UNIX timestamp
	 */
	public void setExpiryDate(long dt) {
	    _expiryDate = Math.max(_loginDate, dt);
	}
	
	/**
	 * Sets the login date.
	 * @param dt the login date as a 64-bit UNIX timestamp
	 */
	public void setLoginDate(long dt) {
		_loginDate = Math.max(1, dt);
	}
    
 	/**
 	 * Updates the user's IP address.
 	 * @param remoteAddr the IP address
 	 */
 	public void setRemoteAddr(String remoteAddr) {
 		_remoteAddr = remoteAddr;
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder buf = new StringBuilder(_userID);
 		buf.append('@');
 		buf.append(_remoteAddr);
 		return buf.toString();
 	}
}