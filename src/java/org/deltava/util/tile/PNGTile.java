// Copyright 2006, 2007, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

import java.io.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * A Tile that stores pre-compressed PNG data. 
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class PNGTile extends AbstractTile implements CompressedTile {
	
	private static final long serialVersionUID = 1907204547140780918L;
	
	private byte[] _imgData;

	/**
	 * Deserialization constructor.
	 */
	public PNGTile() {
		super(null);
	}
	
	/**
	 * Creates a new PNG tile.
	 * @param addr the Tile address.
	 */
	public PNGTile(TileAddress addr) {
		super(addr);
	}
	
	/**
	 * Creates a new PNG tile from an existing tile.
	 * @param addr the TileAddress
	 * @param img the BufferedImage
	 */
	public PNGTile(TileAddress addr, java.awt.image.BufferedImage img) {
		super(addr);
		setImage(img);
	}

	/**
	 * Sets the image data. This will convert the rendered image to PNG format.
	 * @param img the image to convert.
	 * @see Tile#setImage(BufferedImage)
	 */
	@Override
	public void setImage(BufferedImage img) {
		try (ByteArrayOutputStream pngData = new ByteArrayOutputStream(2048)) {
			ImageIO.write(img, "png", pngData);
			_imgData = pngData.toByteArray();
		} catch (IOException ie) {
			// should never occur
		}
	}
	
	/**
	 * Sets the image data.
	 * @param data the image data
	 */
	public void setImage(byte[] data) {
		_imgData = data;
	}
	
	/**
	 * Returns the compressed image data.
	 * @return the binary image data
	 */
	@Override
	public byte[] getData() {
		return _imgData;
	}
}