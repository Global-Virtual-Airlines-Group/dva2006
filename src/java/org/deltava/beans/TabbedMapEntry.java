// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.List;

/**
 * An interface to define icon formatting for entries that can be displayed in Google Maps v2
 * with tabbed infoboxes.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface TabbedMapEntry extends MapEntry {

	/**
	 * Returns the tab names. 
	 * @return a List of tab names
	 */
	public List<String> getTabNames();
	
	/**
	 * Returns the tab contents.
	 * @return a List of tab contents
	 */
	public List<String> getTabContents();
}