package org.deltava.util.tile;

import java.nio.file.*;
import java.io.IOException;
import java.awt.image.BufferedImage;

import org.gvagroup.tile.*;

import junit.framework.TestCase;

public class TestWindPalette extends TestCase {

	@SuppressWarnings("static-method")
	public void testPalette() throws IOException {
		
		TilePlotter pal = new GFSTilePlotter();
		BufferedImage img = new BufferedImage(Tile.WIDTH * 8, Tile.HEIGHT, BufferedImage.TYPE_INT_ARGB);
		for (int spd = 20; spd < 256; spd++) {
			int c = pal.plot(spd);
			for (int x = (spd * 8); x < ((spd+1) * 8); x++) {
				for (int y = 0; y < img.getHeight(); y++)
					img.setRGB(x, y, c);
			}
		}
		
		PNGTile pt = new PNGTile(new TileAddress(1,1,1), img);
		assertNotNull(pt);
		
		Path p = Path.of("C:\\Temp", "pal.png");
		assertNotNull(p);
		Files.write(p, pt.getData(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.SYNC);
	}
}