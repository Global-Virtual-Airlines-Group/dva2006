// Copyright 2006, 2007 The Weather Channel Interactive. All Rights Reserved.
package org.deltava.util.tile;

import java.awt.Rectangle;
import java.awt.image.*;

/**
 * A single Tile bean.
 * @author LKolin
 * @version 1.0
 * @since 1.0
 */

public class SingleTile extends AbstractTile implements RasterTile {

	private static final IndexColorModel EMPTY_PAL = new IndexColorModel(1, 1, new byte[] {0}, new byte[] {0}, new byte[] {0}, 0);
	private static final BufferedImage EMPTY = new BufferedImage(Tile.WIDTH, Tile.HEIGHT, BufferedImage.TYPE_BYTE_INDEXED, EMPTY_PAL);
	protected BufferedImage _img;
	
	/*
	 * We use a Boolean object instead of the primitive so that we can have an uninitialized null value.
	 */
	private Boolean _isEmpty = null;
	
	/**
	 * Creates a new Tile.
	 * @param addr the address
	 */
	public SingleTile(TileAddress addr) {
		super(addr);
		_img = new BufferedImage(Tile.WIDTH, Tile.HEIGHT, BufferedImage.TYPE_BYTE_INDEXED, EMPTY_PAL);
		_img.setData(EMPTY.getData());
	}

	/**
	 * Sets the Tile image.
	 * @param img the Image
	 * @throws IllegalArgumentException if the height and width are not equal to that of a single tile
	 */
	public void setImage(BufferedImage img) {
		if ((img != null) && ((img.getWidth() != Tile.WIDTH) || (img.getHeight() != Tile.HEIGHT)))
			throw new IllegalArgumentException("Invalid image size " + img.getWidth() + "x" + img.getHeight());

		_img.flush();
		_img = img;
	}

	/**
	 * Returns the Tile image.
	 * @return the image
	 */
	public BufferedImage getImage() {
		return _img;
	}

	/**
	 * Helper method to validate if a raster is empty.
	 * @param r the Raster to check
	 * @return TRUE if the Raster is empty, otherwise FALSE
	 */
	private boolean checkRaster(Raster r) {
		boolean isEmpty = true;
		
		// Get the image size
		int imgSize = r.getWidth() * r.getHeight();
		int sampleSize = r.getNumBands();
		
		// Check if empty
		int[] data = r.getPixels(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight(), new int[sampleSize * imgSize]);
		for (int px = 0; isEmpty && (px < data.length); px++)
			isEmpty &= (data[px] == 0);
		
		return isEmpty;
	}
	
	/**
	 * Returns whether this Tile has any data.
	 * @return TRUE if the Tile has no data, otherwise FALSE
	 */
	public boolean isEmpty() {
		if (_isEmpty != null)
			return _isEmpty.booleanValue();
		
		// Get the image data
		_isEmpty = Boolean.valueOf(checkRaster(_img.getData()));
		return _isEmpty.booleanValue();
	}
	
	/**
	 * Returns whether a child of this Tile would be empty. This has the significant performance advantage since
	 * less data needs to be processed than if one generated the Tile and called isEmpty on it.
	 * @param addr the child Address
	 * @return FALSE if the Tile would be empty
	 * @throws IllegalArgumentException if addr's parent is not this Tile
	 * @throws NullPointerException if addr is null
	 */
	public boolean isEmpty(TileAddress addr) {
		TileAddress parent = addr.zoomTo(_addr.getLevel());
		if (_addr.compareTo(parent) != 0)
			throw new IllegalArgumentException("Invalid parent - " + parent);
		
		// Calculate the X/Y coordinates on the current pixel
		int levelChange = (addr.getLevel() - _addr.getLevel());
		int baseX = (addr.getPixelX() >> levelChange) - _addr.getPixelX();
		int baseY = (addr.getPixelY() >> levelChange) - _addr.getPixelY();
		
		// Build the raster
		Raster data = _img.getData(new Rectangle(baseX, baseY,(Tile.WIDTH >> levelChange), (Tile.HEIGHT >> levelChange)));
		return checkRaster(data);
	}
}