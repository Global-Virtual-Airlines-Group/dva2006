// Copyright 2006, 2007 The Weather Channel Interactive. All Rights Reserved.
package org.deltava.util.tile;

import java.awt.image.BufferedImage;

/**
 * A marker interface for Tiles that contain Raster data.
 * @author LKolin
 * @version 1.0
 * @since 1.0
 */

public interface RasterTile extends Tile {

	/**
	 * Returns the rendered image.
	 * @return the image
	 */
	public BufferedImage getImage();
}