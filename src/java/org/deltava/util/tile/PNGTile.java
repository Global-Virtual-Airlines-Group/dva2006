// Copyright 2006, 2007, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

import java.io.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * A Tile that stores pre-compressed PNG data. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class PNGTile extends AbstractTile implements CompressedTile, java.io.Externalizable {
	
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
	 * @param st the existing tile
	 */
	public PNGTile(SingleTile st) {
		super(st.getAddress());
		setImage(st.getImage());
	}

	/**
	 * Sets the image data. This will convert the rendered image to PNG format.
	 * @param img the image to convert.
	 * @see Tile#setImage(BufferedImage)
	 */
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
	public byte[] getData() {
		return _imgData;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(_addr.getX());
		out.writeInt(_addr.getY());
		out.writeInt(_addr.getLevel());
		out.writeInt(_imgData.length);
		out.write(_imgData);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		_addr = new TileAddress(in.readInt(), in.readInt(), in.readInt());
		_imgData = new byte[in.readInt()];
		in.read(_imgData);
	}
}