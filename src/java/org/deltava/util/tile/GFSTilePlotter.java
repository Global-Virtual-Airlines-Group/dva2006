// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

/**
 * A pixel plotter for jet stream tiles.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class GFSTilePlotter implements TilePlotter {

	@Override
	public int plot(int js) {
		int c = Math.min(255, js + 20);
		if (js > 55) {
			int r = Math.min(255, c+24);
			int g = (js > 120) ? Math.min(255, c+32): c;
			int argb = 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8)  | c;
			return argb;
		}

		c += 16;
		return 0xFF000000 | (c << 16) | (c << 8) | c;
	}
}