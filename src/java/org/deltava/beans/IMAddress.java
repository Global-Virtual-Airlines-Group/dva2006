// Copyright 2006, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration to store Instant Messaging constants.
 * @author Luke
 * @version 3.4
 * @since 1.0
 */

public enum IMAddress {
	AIM("AOL", true), MSN("MSN", true), ICQ("ICQ", true), YIM("Yahoo", true), FB("Facebook", false),
		FBTOKEN("Facebook Token", false);
	
	private String _name;
	private boolean _visible;
	
	IMAddress(String name, boolean isVisible) {
		_name = name;
		_visible = isVisible;
	}
	
	/**
	 * Returns the service name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns whether the service address can be viewed and updated by the user. 
	 * @return TRUE if user-visible, otherwise FALSE
	 */
	public boolean getIsVisible() {
		return _visible;
	}
}