// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.color;

import java.util.StringTokenizer;

/**
 * A class to define Google Maps colors.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GoogleMapsColor extends GoogleColor {

	public GoogleMapsColor(int red, int green, int blue) {
		super(red, green, blue, 255);
	}

	public GoogleMapsColor(String colors) {
		super(0, 0, 0, 255);
		StringTokenizer tkns = new StringTokenizer(colors, "-");
		_red = Integer.parseInt(tkns.nextToken());
		_green = Integer.parseInt(tkns.nextToken());
		_blue = Integer.parseInt(tkns.nextToken());
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder('#');
		buf.append(formatHex(_red));
		buf.append(formatHex(_green));
		buf.append(formatHex(_blue));
		return buf.toString();
	}
}