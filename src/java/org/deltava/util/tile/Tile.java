// Copyright 2006, 2007, 2008, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

/**
 * An interface for Tile images.
 * @author Luke
 * @version 11.3
 * @since 11.3
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
	public void setImage(java.awt.image.BufferedImage img);
}