// Copyright 2006, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.LinkedHashMap;

/**
 * An interface to define icon formatting for entries that can be displayed in Google Maps v2
 * with tabbed infoboxes.
 * @author Luke
 * @version 8.3
 * @since 1.0
 */

public interface TabbedMapEntry extends MapEntry {

	/**
	 * Returns the tab names. 
	 * @return a List of tab names
	 */
	public LinkedHashMap<String, String> getTabs();
}