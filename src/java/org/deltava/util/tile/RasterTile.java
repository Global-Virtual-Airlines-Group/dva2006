// Copyright 2006, 2007, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

/**
 * An interface for Tiles that contain Raster data.
 * @author Luke
 * @version 7.0
 * @since 5.0
 */

public interface RasterTile extends org.gvagroup.tile.Tile {

	/**
	 * Returns the rendered image.
	 * @return the image
	 */
	public java.awt.image.BufferedImage getImage();
}