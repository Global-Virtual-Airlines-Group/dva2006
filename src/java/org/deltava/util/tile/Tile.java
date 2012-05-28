// Copyright 2006, 2007, 2008 The Weather Channel Interactive. All Rights Reserved.
package org.deltava.util.tile;

import java.awt.image.BufferedImage;

/**
 * Tiles are the core of the TileServer application.
 * @author LKolin
 * @version 2.1
 * @since 1.0
 */

public interface Tile extends Comparable<Tile> {
	
	/**
	 * Default tile height in pixels.
	 */
	public static final int HEIGHT = 256;
	
	/**
	 * Default tile width in pixels.
	 */
	public static final int WIDTH = 256;
	
	/**
	 * Default mobile tile height in pixels.
	 */
	public static final int MOB_HEIGHT = 64;
	
	/**
	 * Default mobile tile width in pixels.
	 */
	public static final int MOB_WIDTH = 64;
	
	/**
	 * Returns the Tile address.
	 * @return the address
	 */
	public TileAddress getAddress();

	/**
	 * Returns the Tile name
	 * @return the name
	 */
	public String getName();
	
	/**
	 * Sets the tile image.
	 * @param img the rendered image
	 */
	public void setImage(BufferedImage img);
}