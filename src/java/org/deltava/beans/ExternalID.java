// Copyright 2006, 2010, 2012, 2017, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration to store external ID types and Facebook tokens.
 * @author Luke
 * @version 10.3
 * @since 1.0
 */

public enum ExternalID {
	MSN("MSN", false), ICQ("ICQ", false), YIM("Yahoo", false), FB("Facebook", false), FBTOKEN("Facebook Token", false), FBPAGE("Facebook page", false), NAVIGRAPH("Navigraph", true), DISCORD("Discord", true);
	
	private final String _name;
	private final boolean _visible;
	
	ExternalID(String name, boolean isVisible) {
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