// Copyright 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to define beans that have an RGB color. 
 * @author Luke
 * @version 11.4
 * @since 11.1
 */

public interface RGBColor {

	/**
	 * Returns the RGB color.
	 * @return a 24-bit RGB color value
	 */
	public int getColor();
	
	/**
	 * Returns the RGB color as a 24-bit hexadecimal string.
	 * @return a hexadecimal RGB color value
	 */
	default String getHexColor() {
		StringBuilder buf = new StringBuilder(Integer.toHexString(getColor()).toLowerCase());
		while (buf.length() < 6)
			buf.insert(0, '0');
		
		return buf.toString();
	}
}