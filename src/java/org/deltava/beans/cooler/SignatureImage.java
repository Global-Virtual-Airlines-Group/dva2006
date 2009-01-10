// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.cooler;

import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.deltava.beans.DatabaseBlobBean;

/**
 * A bean to store Signature Images for processing.
 * @author Luke
 * @version 2.3
 * @since 2.3
 */

public class SignatureImage extends DatabaseBlobBean {
	
	private BufferedImage _img;
	private boolean _isApproved;
	
	/**
	 * Initializes the bean.
	 * @param id the Pilot's database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 */
	public SignatureImage(int id) {
		super();
		setID(id);
	}
	
	/**
	 * Returns the image height.
	 * @return the height in pixels, or -1 if not loaded
	 */
	public int getHeight() {
		return (_img == null) ? -1 : _img.getHeight();
	}
	
	/**
	 * Returns the image width.
	 * @return the width in pixels, or -1 if not loaded
	 */
	public int getWidth() {
		return (_img == null) ? -1 : _img.getWidth();
	}

	/**
	 * Loads the image from a byte array and updates the internal buffer.
	 * @param buffer the image buffer
	 * @throws IllegalArgumentException if the image cannot be parsed
	 */
	public void load(byte[] buffer) {
		super.load(buffer);
		try {
			updateImage();
		} catch (IOException ie) {
			throw new IllegalArgumentException(ie);
		}
	}
	
	/**
	 * Loads the image from a stream.
	 */
	public void load(InputStream is) throws IOException {
		super.load(is);
		updateImage();
	}
	
	private void updateImage() throws IOException {
		_isApproved = false;
		if (isLoaded())
			_img = ImageIO.read(getInputStream());
	}
	
	/**
	 * Marks the signature with a watermark. 
	 * @param txt the watermark text
	 * @param loc the location of the text
	 */
	public void watermark(String txt, Point loc) {
		if (_isApproved || (_img == null)) 
			return;
		
		// Get the color
		int whiteCount = 0;
		for (int x = loc.x; x < Math.min(_img.getWidth() - 1, (loc.x + (txt.length() * 8))); x++) {
			for (int y = loc.y; y > Math.max(0, loc.y - 30); y--) {
				Color c = new Color(_img.getRGB(x, y));
				if ((c.getBlue() > 192) && (c.getRed() > 192) && (c.getGreen() > 192))
					whiteCount++;
			}
		}
		
		// Get the percentage that is white
		float whitePct = whiteCount / (30.0f * txt.length() * 8);
		
		// Draw the text
		Graphics2D g = _img.createGraphics();
		g.setColor((whitePct < .71) ? Color.WHITE : Color.BLACK);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.275f));
		g.drawString(txt, loc.x, loc.y);
		g.dispose();
		_isApproved = true;
	}
	
	/**
	 * Retrieves the new image in a particular format.
	 * @param fmt the format code (eg. gif, jpg, png)
	 * @return the formatted image
	 * @throws IOException if an error occurs
	 */
	public byte[] getImage(String fmt) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream(16384);
		ImageIO.write(_img, fmt.toLowerCase(), os);
		_buffer = os.toByteArray();
		return _buffer;
	}
}