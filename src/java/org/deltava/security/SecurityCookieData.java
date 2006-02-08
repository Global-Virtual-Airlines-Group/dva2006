// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.Map;

/**
 * A bean containing data stored in the security cookie.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SecurityCookieData {
    
    private static final long DEFAULT_EXPIRY = 360 * 60000; // 6 hours
    
 	private String _userID;
	private String _pwd;
	private String _remoteAddr;
	private long _expiryDate;
	
	private int _screenX = 1024;
	private int _screenY = 768;

	/**
	 * Creates security cookie data for a given user ID.
	 * @param userID the user ID
	 */
    public SecurityCookieData(String userID) {
        super();
        _userID = userID;
        setExpiryDate(System.currentTimeMillis() + SecurityCookieData.DEFAULT_EXPIRY);
    }
    
    /**
     * Creates a new security cookie data bean.
     * @param cookieData a map of parameters
     */
    public SecurityCookieData(Map<String, String> cookieData) {
        this(cookieData.get("uid"));
        _remoteAddr = cookieData.get("addr");
    }
    
    /**
     * Returns the expiration date of the cookie.
     * @return the expiration date as a 64-bit Unix timestamp
     */
    public long getExpiryDate() {
        return _expiryDate;
    }
    
    /**
     * Returns the user's screen width.
     * @return the width in pixels
     */
    public int getScreenX() {
       return _screenX;
    }
    
    /**
     * Returns the user's screen height.
     * @return the height in pixels
     */
    public int getScreenY() {
       return _screenY;
    }
    
 	/**
 	 * Retrieves the user ID from this security cookie.
 	 * @return the Directory Name of the user
 	 */
 	public String getUserID() {
 		return _userID;
 	}
 	
 	/**
 	 * Retrieves the password from this security cooke. This value is not guaranteed to be set, and this method
 	 * exists for use in SSO where we need to get the password
 	 * @return the user's password
 	 */
 	public String getPassword() {
 		return _pwd;
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
	 * @param expiryDate the expiration date as a 32-bit UNIX timestamp
	 */
	public void setExpiryDate(long expiryDate) {
	    if (expiryDate < 1)
	        throw new IllegalArgumentException("Expiration Date cannot be zero or negative");
	    
	    _expiryDate = expiryDate;
	}
    
    /**
 	 * Updates the user's password.
 	 * @param pwd the new password
 	 */
 	public void setPassword(String pwd) {
 		_pwd = pwd;
 	}
 	
 	/**
 	 * Updates the user's IP address.
 	 * @param remoteAddr the IP address
 	 */
 	public void setRemoteAddr(String remoteAddr) {
 		_remoteAddr = remoteAddr;
 	}
 	
 	/**
 	 * Updates the user's screen size.
 	 * @param width the width in pixels
 	 * @param height the height in pixels
 	 */
 	public void setScreenSize(int width, int height) {
 	   _screenX = width;
 	   _screenY = height;
 	}
 	
	/**
	 * Helper method to turn the bytes in the password into hexadecimal values.
	 * @return password converted to hex bytes
	 */
	String getPasswordBytes() {
		StringBuilder buf = new StringBuilder();
		for (int x = 0; x < _pwd.length(); x++)
			buf.append(Integer.toHexString(_pwd.charAt(x)));
		
		return buf.toString();
	}
}