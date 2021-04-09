// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

/**
 * An interface for calculating pixel values on raster tiles. 
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public interface TilePlotter {

	/**
	 * Calculates a pixel value.
	 * @param value the source data value
	 * @return the ARGB color for the pixel.
	 */
	public int plot(int value);
}