// Copyright 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.color;

/**
 * A class to define Google Earth colors.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class GoogleEarthColor extends GoogleColor {
	
	/**
	 * Creates a Google Earth color.
	 * @param red the RGB red value
	 * @param green the RGB green value
	 * @param blue the RGB blue value
	 * @param alpha the transparency value
	 */
	public GoogleEarthColor(int red, int green, int blue, int alpha) {
		super(red, green, blue, alpha);
	}
	
	/**
	 * Creates a Google Earth color with default semi-transparency (128).
	 * @param red the RGB red value
	 * @param green the RGB green value
	 * @param blue the RGB blue value
	 */
	public GoogleEarthColor(int red, int green, int blue) {
		super(red, green, blue, 128);
	}

	/**
	 * Dims the color.
	 * @param factor the factor to divide all intensities by
	 * @return a dimmed clone of the color
	 */
	public GoogleEarthColor dim(float factor) {
		return new GoogleEarthColor(Math.round(_red / factor), Math.round(_green / factor),
				Math.round(_blue / factor), Math.round(_alpha / factor));
	}

	/**
	 * Renders the color to a string, suitable for insertion in KML documents.
	 * @return the KML-formatted color value
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(formatHex(_alpha));
		buf.append(formatHex(_blue));
		buf.append(formatHex(_green));
		buf.append(formatHex(_red));
		return buf.toString();
	}
}