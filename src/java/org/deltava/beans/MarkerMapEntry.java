// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to define entries that can be displayed in Google Maps using a standard marker.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public interface MarkerMapEntry extends MapEntry {

	/**
	 * Returns the icon color for this entry if displayed in a Google Map.
	 * @return the icon color
	 */
	public String getIconColor();
}