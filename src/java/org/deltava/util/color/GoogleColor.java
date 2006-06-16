// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.color;

import org.deltava.util.StringUtils;

/**
 * An abstract class to render RGB values as Google product colors. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class GoogleColor {

	/**
	 * The RGB blue value.
	 */
	protected int _blue;
	
	/**
	 * The RGB green value.
	 */
	protected int _green;
	
	/**
	 * The RGB red value.
	 */
	protected int _red;
	
	/**
	 * The transparency value.
	 */
	protected int _alpha;
	
	/**
	 * Initializes the color bean.
	 * @param red the red intensity, from 0 to 255
	 * @param green the geen intensity, from 0 to 255
	 * @param blue the blue intensity, from 0 to 255
	 * @param alpha the transparency, from 0 to 255
	 */
	protected GoogleColor(int red, int green, int blue, int alpha) {
		super();
		_red = (red > 255) ? 255 : red;
		_green = (green > 255) ? 255: green;
		_blue = (blue > 255) ? 255 : blue;
		_alpha = (alpha > 255) ? 255 : alpha;
	}
	
	/**
	 * Initializes the color bean
	 * @param red the red intensity, from 0 to 255
	 * @param green the geen intensity, from 0 to 255
	 * @param blue the blue intensity, from 0 to 255
	 * @param alpha the transparancy, as a fraction between 0 and 1
	 */
	protected GoogleColor(int red, int green, int blue, float alpha) {
		this(red, green, blue, Math.round(alpha * 255));
	}
	
	/**
	 * Returns the red intensity.
	 * @return the red intensity value, from 0 to 255
	 */
	public int getRed() {
		return _red;
	}
	
	/**
	 * Returns the green intensity.
	 * @return the green intensity value, from 0 to 255
	 */
	public int getGreen() {
		return _green;
	}
	
	/**
	 * Returns the blue intensity.
	 * @return the blue intensity value, from 0 to 255
	 */
	public int getBlue() {
		return _blue;
	}
	
	/**
	 * Returns the transparency.
	 * @return the transparency value, from 0 to 255
	 */
	public int getAlpha() {
		return _alpha;
	}
	
	/**
	 * Helper method to generate a hex string without 0x and two characters long.
	 * @param value the RGB value
	 * @return the hex representation of the value, with a leading zero if required
	 */
	protected String formatHex(int value) {
		String tmp = StringUtils.formatHex(value).substring(2);
		return (tmp.length() == 1) ? "0" + tmp : tmp;
	}
}