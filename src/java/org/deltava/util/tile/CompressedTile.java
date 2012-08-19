// Copyright 2006, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

/**
 * An interface for Tiles that do not contain raster data.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public interface CompressedTile extends Tile {

	/**
	 * Returns the compressed image data.
	 * @return the image data
	 */
	public byte[] getData();
}
