// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.lang.reflect.Field;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import org.deltava.util.cache.*;
import org.deltava.util.ConfigLoader;
import org.deltava.util.tile.Palette;

/**
 * A Web Service to generate custom aircraft icons. 
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class AircraftIconService extends WebService {
	
	private static final Cache<CacheableBlob> _cache = CacheManager.get(CacheableBlob.class, "AircraftIcon");
	
	private final Color TX = new Color(255, 0, 255);
	
	/*
	 * Helper method to load colors by name.
	 */
	private static Color getColor(String name) throws Exception { 
		Field f = Color.class.getField(name.toUpperCase());
		return (f != null) ? (Color) f.get(null) : null;
	}

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the color
		String color = ctx.getParameter("c"); int c = 0;
		try {
			// Parse the color
			if (color.startsWith("x") && (color.length() == 7)) {
				int r = Integer.parseInt(color.substring(1, 3), 16);
				int g = Integer.parseInt(color.substring(3, 5), 16);
				int b = Integer.parseInt(color.substring(5, 7), 16);
				c = (r << 16) + (g << 8) + b;
			} else if (Character.isDigit(color.charAt(0)))
				c = Integer.parseInt(color) & 0xFFFFFF;
			else
				c = getColor(color).getRGB();
		} catch (Exception e) {
			return SC_BAD_REQUEST;
		}
		
		// Check the cache and generate the image if needed
		CacheableBlob b = _cache.get(Integer.valueOf(c));
		if (b == null) {
			try (InputStream is = ConfigLoader.getStream("/etc/acIcon.bmp")) {
				BufferedImage img = ImageIO.read(is);

				// Apply color
				for (int y = 0; y < img.getHeight(); y++) {
					for (int x = 0; x < img.getWidth(); x++) {
						int pc = img.getRGB(x, y) & 0xFFFFFF;
						if (pc == 0xFF0000)
							img.setRGB(x, y, c);
					}
				}
				
				// Shrink the palette
				Palette p = new Palette(img, 256);
				p.setTransparent(TX);
				BufferedImage outImg = p.translate(img, true, true);

				// Convert to PNG
				ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
				ImageIO.write(outImg, "png", out);
				b = new CacheableBlob(Integer.valueOf(c), out.toByteArray());
				_cache.add(b);
			} catch (IOException ie) {
				throw error(SC_INTERNAL_SERVER_ERROR, ie.getMessage(), false);
			}
		}

		try {
			ctx.setContentType("image/png");
			ctx.setHeader("Content-Length", b.size());
			ctx.setExpiry(60);
			ctx.getResponse().getOutputStream().write(b.getData());
		} catch (IOException ie) {
			throw error(SC_INTERNAL_SERVER_ERROR, ie.getMessage(), false);
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
		}
		
		return SC_OK;
	}

   @Override
	public boolean isLogged() {
	   return false;
   }
}