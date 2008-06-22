// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to define Google Maps entries displayed using a Google Earth icon. 
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public interface IconMapEntry extends MapEntry {

	/**
	 * Returns the Google Earth palette code.
	 * @return the palette code
	 */
	public int getPaletteCode();
	
	/**
	 * Returns the Google Earth icon code.
	 * @return the icon code within the palette
	 */
	public int getIconCode();
}