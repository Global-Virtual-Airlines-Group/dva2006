// Copyright 2006, 2007, 2012, 2016, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

/**
 * An abstract class to support common tile functions.
 * @author Luke
 * @version 11.3
 * @since 11.3
 */

public abstract class AbstractTile implements Tile, java.io.Serializable {
	
	private static final long serialVersionUID = -7740652028060955205L;
	
	protected final TileAddress _addr;
	
	/**
	 * Creates a new Tile.
	 * @param addr the Tile address
	 */
	protected AbstractTile(TileAddress addr) {
		super();
		_addr = addr;
	}
	
	@Override
	public final TileAddress getAddress() {
		return _addr;
	}
	
	@Override
	public final String getName() {
		return _addr.getName();
	}

	@Override
	public int compareTo(Tile t2) {
		return _addr.compareTo(t2.getAddress());
	}

	@Override
	public int hashCode() {
		return _addr.hashCode();
	}
}