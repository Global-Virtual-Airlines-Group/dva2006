// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

import org.deltava.util.URLParser;
import org.deltava.util.tile.*;

/**
 * A servlet to display Quad-tree tiles.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

abstract class TileServlet extends GenericServlet {

	protected byte[] EMPTY;
	
	/**
	 * A class to store Tile Addresses including type name and date.
	 */
	protected class TileAddress5D extends TileAddress {
		private final String _name;
		private final Date _dt;
		
		TileAddress5D(String type, Date effDate, String name) {
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
		public Date getDate() {
			return _dt;
		}
		
		public int hashCode() {
			return toString().hashCode();
		}
		
		public String toString() {
			StringBuilder buf = new StringBuilder(_name).append(':');
			buf.append((_dt == null) ? 0 : _dt.getTime() / 1000).append(':');
			return buf.append(getName()).toString();
		}
	}
	
	/**
	 * Initializes the servlet.
	 */
	@Override
	public void init() {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream(1024)) {
			SingleTile st = new SingleTile(new TileAddress(0, 0, 1));
			ImageIO.write(st.getImage(), "png", out);
			EMPTY = out.toByteArray();
		} catch (IOException ie) {
			// NOOP
		}
	}

	/**
	 * Parses a URI to get the five-dimensional tile address.
	 * @param uri the URI
	 * @return a TileAddress5D
	 */
	protected TileAddress5D getTileAddress(String uri) {
		
		// Parse the URL and get the path parts
		URLParser url = new URLParser(uri);
		LinkedList<String> pathParts = url.getPath();
		Collections.reverse(pathParts);
		
		long rawDate = Long.parseLong(pathParts.poll());
		return new TileAddress5D(pathParts.poll(), new Date(rawDate), url.getName());
	}
}