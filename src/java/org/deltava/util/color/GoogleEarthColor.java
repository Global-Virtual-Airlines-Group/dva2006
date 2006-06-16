// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.color;

/**
 * A class to define Google Earth colors.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GoogleEarthColor extends GoogleColor {
	
	public GoogleEarthColor(int red, int green, int blue, int alpha) {
		super(red, green, blue, alpha);
	}
	
	public GoogleEarthColor(int red, int green, int blue) {
		super(red, green, blue, 128);
	}

	public GoogleEarthColor dim(float factor) {
		return new GoogleEarthColor(Math.round(_red / factor), Math.round(_green / factor),
				Math.round(_blue / factor), Math.round(_alpha / factor));
	}

	public String toString() {
		StringBuilder buf = new StringBuilder(formatHex(_alpha));
		buf.append(formatHex(_blue));
		buf.append(formatHex(_green));
		buf.append(formatHex(_red));
		return buf.toString();
	}
}