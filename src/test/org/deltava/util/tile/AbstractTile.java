// Copyright 2006, 2007 The Weather Channel Interactive. All Rights Reserved.
package org.deltava.util.tile;

import java.awt.image.*;

/**
 * An abstract class to support common tile functions.
 * @author LKolin
 * @version 1.0
 * @since 1.0
 */

public abstract class AbstractTile implements Tile {
	
	protected TileAddress _addr;
	
	/**
	 * Creates a new Tile.
	 * @param addr the Tile address
	 */
	protected AbstractTile(TileAddress addr) {
		super();
		_addr = addr;
	}
	
	/**
	 * Returns the Tile address.
	 * @return the address
	 */
	public final TileAddress getAddress() {
		return _addr;
	}
	
	/**
	 * Returns the Tile name.
	 * @return the name
	 */
	public final String getName() {
		return _addr.getName();
	}
	
	/**
	 * Updates the Tile image.
	 * @param img the Tile image
	 */
	public abstract void setImage(BufferedImage img);

	/**
	 * Compares two tiles by comparing their addresses.
	 */
	public int compareTo(Tile t2) {
		return _addr.compareTo(t2.getAddress());
	}
}