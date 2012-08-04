// Copyright 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

/**
 * A bean to store data about the current HTTP request. 
 * @author Luke
 * @version 4.2
 * @since 3.7
 */

public class HTTPContextData implements java.io.Serializable {
	
	private String _userAgent;
	
	private final OperatingSystem _os;
	private final BrowserType _browser;
	
	private int _major;
	private int _minor;
	private boolean _html5;

	/**
	 * Creates the context data.
	 * @param os the client OperatingSystem
	 * @param browser the client browser type 
	 */
	public HTTPContextData(OperatingSystem os, BrowserType browser) {
		super();
		_os = os;
		_browser = browser;
	}

	/**
	 * Returns the client operating system.
	 * @return an OperatingSystem enum
	 */
	public OperatingSystem getOperatingSystem() {
		return _os;
	}
	
	/**
	 * Returns the client browser type.
	 * @return a BrowserType enum
	 */
	public BrowserType getBrowserType() {
		return _browser;
	}

	/**
	 * Returns the browser major version number.
	 * @return the major version
	 */
	public int getMajor() {
		return _major;
	}
	
	/**
	 * Returns the browser minor version number.
	 * @return the minor version
	 */
	public int getMinor() {
		return _minor;
	}
	
	/**
	 * Returns whether the client browser has sufficient HTML5 support.
	 * @return TRUE if full HTML5 support, otherwise FALSE
	 */
	public boolean getHTML5() {
		return _html5;
	}
	
	/**
	 * Returns the browser's user-agent header.
	 * @return the user-agent header
	 */
	public String getUserAgent() {
		return _userAgent;
	}

	/**
	 * Sets whether the client browser has sufficient HTML5 support.
	 * @param hasHTML5 TRUE if full HTML5 support, otherwise FALSE
	 */
	public void setHTML5(boolean hasHTML5) {
		_html5 = hasHTML5;
	}
	
	/**
	 * Sets the browser version.
	 * @param major the major version number
	 * @param minor the minor version number
	 */
	public void setVersion(int major, int minor) {
		_major = Math.max(1, major);
		_minor = Math.max(0, minor);
	}
	
	/**
	 * Updates the browser's user-agent header.
	 * @param ua the user-agent header
	 */
	public void setUserAgent(String ua) {
		_userAgent = ua;
	}
}