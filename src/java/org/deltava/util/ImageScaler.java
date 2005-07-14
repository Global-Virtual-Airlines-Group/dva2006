// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util;

import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import java.io.*;

/**
 * A utility class to support image scaling.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ImageScaler {

	// see http://www.geocities.com/marcoschmidt.geo/java-save-jpeg-thumbnail.html

	private byte[] _buffer;
	private RenderingHints _hints;

	private int _newX;
	private int _newY;

	/**
	 * Initialize the Image scaler
	 */
	public ImageScaler(byte[] imgData) {
		super();
		_buffer = imgData;
		initRenderingHints();
	}

	/**
	 * Helper method to initialize rendering options.
	 */
	private void initRenderingHints() {
		_hints = new RenderingHints(null);
		_hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
		_hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
		_hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		_hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	}

	/**
	 * Sets the new image size.
	 * @param x the width in pixels
	 * @param y the height in pixels
	 * @see ImageScaler#setImageSize(Dimension)
	 */
	public void setImageSize(int x, int y) {
		_newX = x;
		_newY = y;
	}

	/**
	 * Sets the new image size.
	 * @param d the image dimension
	 * @see ImageScaler#setImageSize(int, int)
	 */
	public void setImageSize(Dimension d) {
		setImageSize((int) d.getWidth(), (int) d.getHeight());
	}

	/**
	 * Returns the scaled image.
	 * @param format the informal name of the format (eg. jpg or png)
	 * @return the scaled image data
	 */
	public byte[] scale(String format) throws IOException {

		// Get the AWT toolkit and create the image
		Image img = Toolkit.getDefaultToolkit().createImage(_buffer);

		// Scale the image down
		Image scImage = new ImageIcon(img.getScaledInstance(_newX, _newY, Image.SCALE_SMOOTH)).getImage();

		// Create the destination image
		BufferedImage newImage = new BufferedImage(_newX, _newY, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = newImage.createGraphics();

		// Draw the scaled image
		g.drawImage(scImage, 0, 0, null);
		g.dispose();

		// Create an output stream to the destination array
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(16384);

		// Render to the output stream and return its data - there should not be an error from this
		ImageIO.write(newImage, format, outStream);
		return outStream.toByteArray();
	}
}