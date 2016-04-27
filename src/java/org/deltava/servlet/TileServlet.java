// Copyright 2012, 2013, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.io.*;
import java.util.*;
import java.time.Instant;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.deltava.util.URLParser;
import org.deltava.util.tile.*;

/**
 * A servlet to display Quad-tree tiles.
 * @author Luke
 * @version 7.0
 * @since 5.0
 */

abstract class TileServlet extends GenericServlet {

	protected byte[] EMPTY;
	
	/**
	 * A class to store Tile Addresses including type name and date.
	 */
	protected class TileAddress5D extends TileAddress {
		private final String _name;
		private final Instant _dt;
		
		TileAddress5D(String type, Instant effDate, String name) {
			super(name);
			_name = type;
			_dt = effDate;
		}
		
		/**
		 * Returns the series type.
		 * @return the type name
		 */
		public String getType() {
			return _name;
		}
		
		/**
		 * Returns the effective date.
		 * @return the tile date/time
		 */
		public Instant getDate() {
			return _dt;
		}
		
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(_name).append(':');
			buf.append((_dt == null) ? 0 : _dt.toEpochMilli() / 1000).append(':');
			return buf.append(getName()).toString();
		}
	}
	
	/**
	 * Initializes the servlet.
	 */
	@Override
	public void init() {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream(1024)) {
			SingleTile st = new SingleTile(new TileAddress(1, 1, 1));
			ImageIO.write(st.getImage(), "png", out);
			EMPTY = out.toByteArray();
		} catch (IOException ie) {
			// NOOP
		}
	}

	/**
	 * Parses a URI to get the five-dimensional tile address.
	 * @param uri the URI
	 * @param getDate TRUE if a data should be fetched, otherwise use current Date
	 * @return a TileAddress5D
	 */
	protected TileAddress5D getTileAddress(String uri, boolean getDate) {
		
		// Parse the URL and get the path parts
		URLParser url = new URLParser(uri);
		LinkedList<String> pathParts = url.getPath();
		Collections.reverse(pathParts);
		
		long rawDate = getDate? Long.parseLong(pathParts.poll()) : 0;
		return new TileAddress5D(pathParts.poll(), getDate ? Instant.ofEpochMilli(rawDate) : null, url.getName());
	}
	
	/**
	 * Helper method to dump the tile data to the output stream.
	 * @param rsp the HttpServletResponse
	 * @param data the tile image data
	 */
	protected static void writeTile(HttpServletResponse rsp, byte[] data) {
		
		// Set headers
		rsp.setHeader("Cache-Control", "public");
		rsp.setIntHeader("max-age", 300);
		rsp.setContentType("image/png");
		rsp.setStatus(HttpServletResponse.SC_OK);
		rsp.setContentLength(data.length);
		rsp.setBufferSize(Math.min(65536, data.length + 16));
		
		// Dump the data to the output stream
		try (OutputStream out = rsp.getOutputStream()) {
			out.write(data);
			rsp.flushBuffer();
		} catch (IOException ie) {
			// NOOP
		}
	}
}