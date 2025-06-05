// Copyright 2006, 2007, 2008, 2012, 2013, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

import java.util.*;
import java.awt.Color;
import java.awt.image.*;

/**
 * A class to store color palette data.
 * @author Luke
 * @version 12.0
 * @since 5.0
 */

public class Palette {

	private final Collection<Color> _colors = new LinkedHashSet<Color>();
	private Color _transparent = Color.BLACK;
	private final int _maxSize;
	
	private transient int[] _xlator = null;

	/**
	 * Generates a palette with a maximum number of colors.
	 * @param maxColors the maximum colors
	 */
	Palette(int maxColors) {
		super();
		if (maxColors > 256)
			throw new IllegalArgumentException("Palette size cannot exceed 256 colors!");
		
		_maxSize = Math.max(0, maxColors);
	}
	
	/**
	 * Generates a palette from an existing Image. 
	 * @param img the Image to process
	 * @param maxColors the maximum size of the palette
	 * @throws IllegalArgumentException if the Image has over 256 colors
	 */
	public Palette(BufferedImage img, int maxColors) {
		this(maxColors);
		
		// If this already uses a palette, read it
		if (img.getColorModel() instanceof IndexColorModel icm) {
			_transparent = new Color(icm.getRGB(icm.getTransparentPixel()));
			
			// Load the colors
			for (int x = 0; x < icm.getMapSize(); x++) {
				Color c = new Color(icm.getRed(x), icm.getGreen(x), icm.getBlue(x));
				_colors.add(c);
			}
			
			_colors.remove(_transparent);
			return;
		}
		
		// Copy data from the image
		for (int by = 0; by < img.getHeight(); by++) {
			for (int bx = 0; bx < img.getWidth(); bx++) {
				Color c = new Color(img.getRGB(bx, by) & 0xFFFFFF);
				_colors.add(c);
			}
		}
		
		// Check that we haven't busted the palette size
		if (_colors.size() > _maxSize)
			throw new IllegalArgumentException("Excessive palette size - " + _colors.size());
	}

	/**
	 * Returns the size of the palette.
	 * @return the number of colors
	 */
	public int size() {
		return _colors.size();
	}
	
	/**
	 * Returns the maximum size of the pallette.
	 * @return the maximum number of colors
	 */
	public int getMaxSize() {
		return _maxSize;
	}
	
	/**
	 * Returns the transparent color in this Palette.
	 * @return the transparent color
	 */
	public Color getTransparent() {
		return _transparent;
	}
	
	/**
	 * Updates the transparent color.
	 * @param c the transparent color
	 */
	public void setTransparent(Color c) {
		_transparent = new Color(c.getRGB() & 0xFFFFFF);
		_colors.remove(_transparent);
	}
	
	/**
	 * Downsamples an Image to use this Palette.
	 * @param in the Image to downsample
	 * @param useClosest wether a nearest match algorithm should be used for unknown colors
	 * @param shrinkPalette TRUE if the palette should use the minimum number of bits possible, otherwise FALSE
	 * @return a downsampled BufferedImage
	 */
	public BufferedImage translate(BufferedImage in, boolean useClosest, boolean shrinkPalette) {
		if (in == null) return in;
		
		// Build the translation table
		if (_xlator == null) {
			Color[] colors = new ArrayList<Color>(_colors).toArray(new Color[0]);
			_xlator = new int[colors.length + 1];
			_xlator[0] = _transparent.getRGB() & 0xFFFFFF;
			for (int x = 0; x < colors.length; x++)
				_xlator[x + 1] = colors[x].getRGB() & 0xFFFFFF;
		}
		
		// Build the new image
		IndexColorModel cm = getColorModel(shrinkPalette);
		int imgType = (cm.getMapSize() <= 16) ? BufferedImage.TYPE_BYTE_BINARY : BufferedImage.TYPE_BYTE_INDEXED;
		BufferedImage img = new BufferedImage(in.getWidth(), in.getHeight(), imgType, cm);
		WritableRaster wr = img.getRaster();
		
		// Copy the data
		for (int by = 0; by < img.getHeight(); by++) {
			for (int bx = 0; bx < img.getWidth(); bx++) {
				int pixel = in.getRGB(bx, by) & 0xFFFFFF;
				int idx = useClosest ? translateClosest(pixel) : translate(pixel);
				wr.setSample(bx, by, 0, idx);
			}
		}
		
		return img;
	}
	
	/**
	 * Translates an RGB value into a matching palette index.
	 */
	private int translate(int rgb) {
		for (int x = 0; x< _xlator.length; x++) {
			if (_xlator[x] == rgb)
				return x;
		}
		
		return 0;
	}

	/**
	 * Translates an RGB value into a matching palette index, using the closest value if not found.
	 */
	private int translateClosest(int rgb) {
		int diff = Integer.MAX_VALUE;
		int closest = 0;
		
		// Do the lookup
		for (int x = 0; x< _xlator.length; x++) {
			if (_xlator[x] == rgb)
				return x;
			
			// See if this is the closest
			int rD = Math.abs((rgb & 0xFF) - (_xlator[x] & 0xFF));
			int gD = Math.abs((rgb & 0xFF00) - (_xlator[x] & 0xFF00));
			int bD = Math.abs((rgb & 0xFF0000) - (_xlator[x] & 0xFF0000));
			int newDiff = (rD + gD + bD) >> 2;
			if (newDiff < diff) {
				closest = x;
				diff = newDiff;
			}
		}

		return closest;
	}
	
	/**
	 * Adds an RGB color to the Palette.
	 * @param r the Red component
	 * @param g the Green component
	 * @param b the Blue component 
	 */
	public void add(int r, int g, int b) {
		if (_colors.size() < _maxSize) {
			_colors.add(new Color(r, g, b));
			_xlator = null;
		}
	}
	
	/**
	 * Adds an RGB color to the Palette.
	 * @param c the Color
	 */
	public void add(Color c) {
		add(c.getRed(), c.getGreen(), c.getBlue());
	}
	
	/**
	 * Queries wether this Palette includes a particular RGB color.
	 * @param c the Color
	 * @return TRUE if the Palette contains this color, otherwise FALSE
	 */
	public boolean contains(Color c) {
		return _colors.contains(c) || _transparent.equals(c);
	}
	
	/**
	 * Returns the Colors in this Palette.
	 * @return a Collection of Colors
	 */
	public Collection<Color> getColors() {
		Collection<Color> results = new LinkedHashSet<Color>(_colors);
		results.add(_transparent);
		return results;
	}
	
	/**
	 * Returns an IndexColorModel using this Palette.
	 * @param shrinkPalette TRUE if the Palette should be no longer than required, otherwise FALSE
	 * @return an IndexColorModel
	 */
	public IndexColorModel getColorModel(boolean shrinkPalette) {
		
		// Calculate the smallest palette size
		int pSize = 2;
		int bpp = 1;
		if (shrinkPalette) {
			while (pSize <= _colors.size() && (pSize < 256)) {
				pSize <<= 1;
				bpp++;
			}
		} else {
			pSize = 256;
			bpp = 8;
		}
		
		// Build the arrays
		byte[] r = new byte[pSize];
		byte[] g = new byte[pSize];
		byte[] b = new byte[pSize];
		
		// Save the transparent color
		r[0] = (byte) _transparent.getRed();
		g[0] = (byte) _transparent.getGreen();
		b[0] = (byte) _transparent.getBlue();
		
		int ofs = 1;
		for (Iterator<Color> i = _colors.iterator(); i.hasNext(); ) {
			Color c = i.next();
			r[ofs] = (byte) c.getRed();
			g[ofs] = (byte) c.getGreen();
			b[ofs] = (byte) c.getBlue();
			ofs++;
		}
		
		// Create the color model
		return new IndexColorModel(bpp, pSize, r, g, b, 0);
	}
}