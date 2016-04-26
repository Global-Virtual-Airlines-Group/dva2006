// Copyright 2009, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.cooler;

import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import javax.imageio.*;

import org.deltava.beans.DatabaseBlobBean;

/**
 * A bean to store Signature Images for processing.
 * @author Luke
 * @version 7.0
 * @since 2.3
 */

public class SignatureImage extends DatabaseBlobBean {
	
	private static final int WMIN = 180;
	
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
	@Override
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
	@Override
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
	 * @param tx the x-coordinate of the text
	 * @param ty the y-coordinate of the text
	 */
	public void watermark(String txt, int tx, int ty) {
		if (_isApproved || (_img == null)) 
			return;
		
		// Get the color
		int whiteCount = 0;
		for (int x = tx; x < Math.min(_img.getWidth() - 1, (tx + (txt.length() * 8))); x++) {
			for (int y = ty; y > Math.max(0, ty - 30); y--) {
				Color c = new Color(_img.getRGB(x, y));
				if ((c.getBlue() > WMIN) && (c.getRed() > WMIN) && (c.getGreen() > WMIN))
					whiteCount++;
			}
		}
		
		// Get the percentage that is white
		float whitePct = whiteCount / (240.0f * txt.length());
		
		// Draw the text
		boolean useShadow = (whitePct < .71); 
		Graphics2D g = _img.createGraphics();
		if (useShadow) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.225f));
			g.setColor(Color.BLACK);
			g.drawString(txt, tx+1, ty+1);	
		}
		
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.275f));
		g.setColor(useShadow ? Color.WHITE : Color.BLACK);
		g.drawString(txt, tx, ty);
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