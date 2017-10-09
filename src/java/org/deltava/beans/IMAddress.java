// Copyright 2006, 2010, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration to store Instant Messaging constants and Facebook tokens.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public enum IMAddress {
	MSN("MSN", true), ICQ("ICQ", true), YIM("Yahoo", true), FB("Facebook", false), FBTOKEN("Facebook Token", false), FBPAGE("Facebook page", false);
	
	private final String _name;
	private final boolean _visible;
	
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