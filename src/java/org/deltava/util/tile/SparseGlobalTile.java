// Copyright 2006, 2007, 2008, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

import java.awt.*;
import java.awt.image.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * A classs to store a global tiled image.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class SparseGlobalTile implements Comparable<SparseGlobalTile> {

	protected final ConcurrentMap<TileAddress, BufferedImage> _tiles = new ConcurrentHashMap<TileAddress, BufferedImage>(2048);
	private final int _level;
	
	private boolean _dynPalette;
	
	/**
	 * Creates a new Global image.
	 * @param level the native zoom level
	 */
	public SparseGlobalTile(int level) {
		super();
		_level = level;
	}

	/**
	 * Returns the native zoom level.
	 * @return the zoom level
	 */
	public int getLevel() {
		return _level;
	}
	
	/**
	 * Returns the addresses of all populated tiles.
	 * @return a Collection of TileAddress beans
	 */
	public Collection<TileAddress> getAddresses() {
		return new ArrayList<TileAddress>(_tiles.keySet());
	}
	
	/**
	 * Returns all of the tiles in this global tile.
	 * @return a Collection of SingleTile objects
	 */
	public Collection<SingleTile> getTiles() {
		Collection<SingleTile> results = new ArrayList<SingleTile>();
		for (Map.Entry<TileAddress, BufferedImage> me : _tiles.entrySet()) {
			SingleTile st = new SingleTile(me.getKey());
			st.setImage(me.getValue());
			results.add(st);
		}
		
		return results;
	}
	
	/**
	 * Returns whether the Global tile has a dynamic palette.
	 * @return TRUE if using a dynamic palette, otherwise FALSE
	 */
	public boolean hasDynamicPalette() {
		return _dynPalette;
	}
	
	/**
	 * Sets whether a dynamic Palette should be used on generated tiles.
	 * @param useDynPalette TRUE if a dynamic palette should be used, otherwise FALSE
	 */
	public void setDynamicPalette(boolean useDynPalette) {
		_dynPalette = useDynPalette;
	}
	
	/**
	 * Adds a Tile or SuperTile to the global Tile.
	 * @param t the SuperTile/Tile to add
	 */
	public void add(RasterTile t) {
		TileAddress addr = t.getAddress();
		if (addr.getLevel() != _level)
			throw new IllegalArgumentException("Zoom = " + addr.getLevel() + ", expecting " + _level);
		
		// Get the image
		if (t instanceof SuperTile) {
			SuperTile st = (SuperTile) t;
			for (Iterator<SingleTile> i = st.getTiles().iterator(); i.hasNext(); ) {
				SingleTile sngT = i.next();
				if (!sngT.isEmpty())
					_tiles.put(sngT.getAddress(), sngT.getImage());
			}
		} else if (t instanceof SingleTile)
			_tiles.put(addr, t.getImage());
	}
	
	/**
	 * Returns a particular Tile.
	 * @param addr the address of the requested Tile
	 * @return a SingleTile bean
	 */
	public SingleTile get(TileAddress addr) {
		SingleTile t = new SingleTile(addr);
		if ((addr.getLevel() == _level) && (_tiles.containsKey(addr)))
			t.setImage(_tiles.get(addr));
		else if (addr.getLevel() > _level) {
			TileAddress tlAddr = addr.zoomTo(_level).zoomTo(addr.getLevel());
			int bitShift = addr.getLevel() - _level;
			
			// Calculate the start of the tile in the native resolution tile
			int deltaX = (addr.getPixelX() - tlAddr.getPixelX()) >> bitShift;
			int deltaY = (addr.getPixelY() - tlAddr.getPixelY()) >> bitShift;
			Rectangle r = new Rectangle(deltaX, deltaY, Tile.WIDTH >> bitShift, Tile.HEIGHT >> bitShift);
			
			// Get the Tile; return an empty image if not in the global tile
			BufferedImage subImg = _tiles.get(addr.zoomTo(_level));
			if (subImg == null)
				return t;
			
			// Create the scaled tile
		    BufferedImage outImg = new BufferedImage(Tile.WIDTH, Tile.HEIGHT, BufferedImage.TYPE_INT_ARGB);
		    Graphics2D g = outImg.createGraphics();
		    g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		    g.drawImage(subImg.getSubimage(r.x, r.y, r.width, r.height), 0, 0, Tile.WIDTH, Tile.HEIGHT, null);
		    g.dispose();
		    
		    // Downsample if we have a palette
			if (_dynPalette) {
				try {
					Palette p = new Palette(outImg, 255);
					p.setTransparent(p.getTransparent());
					t.setImage(p.translate(outImg, false, true));
				} catch (IllegalArgumentException iae) {
					t.setImage(outImg);
				}
			} else
				t.setImage(outImg);
		} else if (addr.getLevel() < _level) {
			int bitShift = _level - addr.getLevel();
			int subImgTiles = 1 << bitShift;
			
			// Build the new tile
			TileAddress nativeAddr = addr.zoomTo(_level);
			BufferedImage outImg = new BufferedImage(Tile.WIDTH, Tile.HEIGHT, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = outImg.createGraphics();
		    g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, !_dynPalette ? RenderingHints.VALUE_INTERPOLATION_BICUBIC :
		    	RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			
			// Create the new tile
			for (int x = 0; x < subImgTiles; x++) {
				for (int y = 0; y < subImgTiles; y++) {
					TileAddress cAddr = new TileAddress(nativeAddr.getX() + x, nativeAddr.getY() + y, _level);
					if (_tiles.containsKey(cAddr)) {
						BufferedImage cImg = _tiles.get(cAddr);
						Point p = new Point(x * (Tile.WIDTH >> bitShift), y * (Tile.HEIGHT >> bitShift));
						g.drawImage(cImg, p.x, p.y, (Tile.WIDTH >> bitShift), (Tile.HEIGHT >> bitShift), null);
					}
				}
			}
			
			// Clean up
			g.dispose();
			
		    // Downsample if we have a palette
			if (_dynPalette) {
				try {
					Palette p = new Palette(outImg, 255);
					p.setTransparent(p.getTransparent());
					t.setImage(p.translate(outImg, false, true));
				} catch (IllegalArgumentException iae) {
					t.setImage(outImg);
				}
			} else
				t.setImage(outImg);
		} else
			return null;

		return t;
	}

	/**
	 * Compares two global images by comparing their native zoom levels.
	 */
	public int compareTo(SparseGlobalTile g2) {
		return Integer.valueOf(_level).compareTo(Integer.valueOf(g2._level));
	}
}