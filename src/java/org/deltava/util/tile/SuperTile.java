// Copyright 2006, 2007, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

import java.util.*;
import java.awt.*;
import java.awt.image.*;

/**
 * A bean that stores SuperTile data. A SuperTile is equivalent to a Tile except that the bean will allow different
 * sized Rasters.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class SuperTile extends SingleTile {

	private Collection<TileAddress> _neAddrs;

	/**
	 * Creates a new SuperTile.
	 * @param addr the address of the top-left tile
	 */
	public SuperTile(TileAddress addr) {
		super(addr);
	}

	/**
	 * Updates the SuperTile data. This will be internally converted into a TiledImage if the image's native tile
	 * dimensions do not match.
	 * @param img the Image
	 */
	@Override
	public void setImage(BufferedImage img) {
		_img.flush();
		_img = img;
	}

	/**
	 * Returns the width of the SuperTile in <i>Tiles</i>
	 * @return the number of Tiles wide
	 */
	public int getTilesX() {
		return _img.getWidth() / Tile.WIDTH;
	}

	/**
	 * Returns the height of the SuperTile in <i>Tiles</i>.
	 * @return the number of Tiles high
	 */
	public int getTilesY() {
		return _img.getHeight() / Tile.HEIGHT;
	}

	/**
	 * Returns the Tile addresses contained by this SuperTile.
	 * @param includeEmpty TRUE if empty tiles should be included, otherwise FALSE
	 * @return a Collection of TileAddress beans
	 */
	public Collection<TileAddress> getAddresses(boolean includeEmpty) {
		if ((!includeEmpty) && (_neAddrs != null))
			return new LinkedHashSet<TileAddress>(_neAddrs);
		
		// Build the addresses
		Collection<TileAddress> results = new TreeSet<TileAddress>();
		for (int y = 0; y < _img.getHeight(); y +=Tile.HEIGHT) {
			for (int x = 0; x < _img.getWidth(); x +=Tile.WIDTH) {
				TileAddress addr = new TileAddress(_addr.getX() + (x / Tile.WIDTH), _addr.getY() + (y / Tile.HEIGHT), _addr.getLevel());
				
				// Check for empty tiles
				if (!includeEmpty) {
					Rectangle area = new Rectangle(x, y, Tile.WIDTH, Tile.HEIGHT);
					Raster r = _img.getData(area);

					// Do empty check
					boolean isEmpty = true;
					
					// Get the raster data
					int imgSize = r.getWidth() * r.getHeight();
					int sampleSize = r.getSampleModel().getNumDataElements();
					int[] data = r.getPixels(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight(), new int[sampleSize * imgSize]);
					for (int px = 0; isEmpty && (px < data.length); px++)
						isEmpty &= (data[px] == 0);

					if (!isEmpty)
						results.add(addr);
				} else
					results.add(addr);
			}
		}
		
		// Cache the results
		if (!includeEmpty)
			_neAddrs = new LinkedHashSet<TileAddress>(results);
		
		return results;
	}
	
	/**
	 * Splits this SuperTile into its component tiles.
	 * @return a Collection of SingleTiles
	 */
	public Collection<SingleTile> getTiles() {
		BufferedImage bImg = getImage();
		
		Collection<SingleTile> results = new HashSet<SingleTile>();
		for (int y = 0; y < _img.getHeight(); y +=Tile.HEIGHT) {
			for (int x = 0; x < _img.getWidth(); x +=Tile.WIDTH) {
				TileAddress addr = new TileAddress(_addr.getX() + (x / Tile.WIDTH), _addr.getY() + (y / Tile.HEIGHT), _addr.getLevel());
				SingleTile t = new SingleTile(addr);
				if (((x + Tile.WIDTH) <= bImg.getWidth()) && (((y + Tile.HEIGHT) <= bImg.getHeight()))) {
					t.setImage(bImg.getSubimage(x, y, Tile.WIDTH, Tile.HEIGHT));
					results.add(t);
				}
			}
		}
		
		return results;
	}
}