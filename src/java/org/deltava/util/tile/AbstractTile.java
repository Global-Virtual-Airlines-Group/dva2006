// Copyright 2006, 2007, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

import java.awt.image.*;

/**
 * An abstract class to support common tile functions.
 * @author Luke
 * @version 7.0
 * @since 5.0
 */

public abstract class AbstractTile implements Tile, java.io.Serializable {
	
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
	@Override
	public final TileAddress getAddress() {
		return _addr;
	}
	
	/**
	 * Returns the Tile name.
	 * @return the name
	 */
	@Override
	public final String getName() {
		return _addr.getName();
	}
	
	/**
	 * Updates the Tile image.
	 * @param img the Tile image
	 */
	@Override
	public abstract void setImage(BufferedImage img);

	/**
	 * Compares two tiles by comparing their addresses.
	 */
	@Override
	public int compareTo(Tile t2) {
		return _addr.compareTo(t2.getAddress());
	}

	@Override
	public int hashCode() {
		return _addr.hashCode();
	}
}