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

	protected int _blue;
	protected int _green;
	protected int _red;
	protected int _alpha;
	
	protected GoogleColor(int red, int green, int blue, int alpha) {
		super();
		_red = (red > 255) ? 255 : red;
		_green = (green > 255) ? 255: green;
		_blue = (blue > 255) ? 255 : blue;
		_alpha = (alpha > 255) ? 255 : alpha;
	}
	
	protected GoogleColor(int red, int green, int blue, float alpha) {
		this(red, green, blue, Math.round(alpha * 255));
	}
	
	public int getRed() {
		return _red;
	}
	
	public int getGreen() {
		return _green;
	}
	
	public int getBlue() {
		return _blue;
	}
	
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