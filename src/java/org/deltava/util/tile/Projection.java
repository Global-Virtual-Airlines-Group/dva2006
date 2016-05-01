// Copyright 2007, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

import org.deltava.beans.GeoLocation;

/**
 * An interface to convert latitude/longitude pairs to X/Y coordinates.
 * @author Luke
 * @version 7.0
 * @since 5.0
 */

public interface Projection {

	/**
	 * Returns the zoom level.
	 * @return the zoom level
	 */
	public int getZoomLevel();
	
	/**
	 * Returns the address of a Tile containing the provided point.
	 * @param loc the GeoLocation
	 * @return the TileAddress of the Tile containing this point at the current zoom level
	 */
	public org.gvagroup.tile.TileAddress getAddress(GeoLocation loc);
	
	/**
	 * Returns the pixel address of the provided point on the global canvas.
	 * @param loc the GeoLocation
	 * @return a Point with the pixel coordinates
	 */
	public java.awt.Point getPixelAddress(GeoLocation loc);
	
	/**
	 * Returns the latitude/longitude of a pixel on the global canvas.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return a GeoLocation object
	 */
	public GeoLocation getGeoPosition(int x, int y);
}