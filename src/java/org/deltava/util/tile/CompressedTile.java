// Copyright 2006, 2012, 2016, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

/**
 * An interface for Tiles that do not contain raster data.
 * @author Luke
 * @version 11.3
 * @since 11.3
 */

public interface CompressedTile extends Tile {

	/**
	 * Returns the compressed image data.
	 * @return the image data
	 */
	public byte[] getData();
}