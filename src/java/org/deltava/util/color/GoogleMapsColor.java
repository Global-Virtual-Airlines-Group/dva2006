// Copyright 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.color;

import java.util.StringTokenizer;

/**
 * A class to define Google Maps colors.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class GoogleMapsColor extends GoogleColor {

	/**
	 * Creates a Google Maps color with default opacity (255).
	 * @param red the RGB red value
	 * @param green the RGB green value
	 * @param blue the RGB blue value
	 */
	public GoogleMapsColor(int red, int green, int blue) {
		super(red, green, blue, 255);
	}

	/**
	 * Creates a Google Maps color from an RGB string.
	 * @param colors a dash-delimited string, in format R-G-B
	 * @throws NumberFormatException if the string is invalid
	 * @see org.deltava.beans.MapEntry#LINECOLORS
	 */
	public GoogleMapsColor(String colors) {
		super(0, 0, 0, 255);
		StringTokenizer tkns = new StringTokenizer(colors, "-");
		_red = Integer.parseInt(tkns.nextToken());
		_green = Integer.parseInt(tkns.nextToken());
		_blue = Integer.parseInt(tkns.nextToken());
	}
	
	/**
	 * Renders the color in a format to be passed to the Goolge Maps JavaScript API.
	 * @return the rendered color
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder('#');
		buf.append(formatHex(_red));
		buf.append(formatHex(_green));
		buf.append(formatHex(_blue));
		return buf.toString();
	}
}