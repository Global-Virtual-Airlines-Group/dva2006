// Copyright 2011, 2012, 2013, 2014, 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

/**
 * A bean to store data about the current HTTP request. 
 * @author Luke
 * @version 10.0
 * @since 3.7
 */

public class HTTPContextData implements java.io.Serializable {
	
	private final OperatingSystem _os;
	private final BrowserType _browser;
	private final DeviceType _device;
	
	private int _major;
	private int _minor;
	private boolean _html5;
	private boolean _ipV6;
	private boolean _http2;

	/**
	 * Creates the context data.
	 * @param os the client OperatingSystem
	 * @param browser the client browser type 
	 * @param dev the device type
	 */
	public HTTPContextData(OperatingSystem os, BrowserType browser, DeviceType dev) {
		super();
		_os = os;
		_browser = browser;
		_device = dev;
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
	 * Rerturns the device type.
	 * @return a DeviceType enum
	 */
	public DeviceType getDeviceType() {
		return _device;
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
	public boolean isHTML5() {
		return _html5;
	}
	
	/**
	 * Returns whether the request was made using IPv6.
	 * @return TRUE if using IPv6, otherwise FALSE
	 */
	public boolean isIPv6() {
		return _ipV6;
	}
	
	/**
	 * Returns whether the request was made using HTTP/2.
	 * @return TRUE if using HTTP/2, otherwise FALSE
	 */
	public boolean isHTTP2() {
		return _http2;
	}
	
	/**
	 * Sets whether the client browser has sufficient HTML5 support.
	 * @param hasHTML5 TRUE if full HTML5 support, otherwise FALSE
	 */
	public void setHTML5(boolean hasHTML5) {
		_html5 = hasHTML5;
	}
	
	/**
	 * Sets whether the request is made using IPv6.
	 * @param isIPv6 TRUE if using IPv6, otherwise FALSE
	 */
	public void setIPv6(boolean isIPv6) {
		_ipV6 = isIPv6;
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
	 * Sets whether this request is served via HTTP/2.
	 * @param isHTTP2 TRUE if using HTTP/2, otherwise FALSE
	 */
	public void setHTTP2(boolean isHTTP2) {
		_http2 = isHTTP2;
	}
}