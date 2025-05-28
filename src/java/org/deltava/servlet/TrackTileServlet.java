// Copyright 2012, 2013, 2014, 2016, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import javax.servlet.http.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.system.VersionInfo;

import org.deltava.dao.GetImage;

import org.deltava.util.*;
import org.deltava.util.tile.TileAddress;
import org.deltava.util.system.SystemData;

import org.gvagroup.pool.*;

/**
 * A servlet to display ACARS track tiles.
 * @author Luke
 * @version 12.0
 * @since 5.0
 */

public class TrackTileServlet extends TileServlet {

	private static final Logger log = LogManager.getLogger(TrackTileServlet.class);
	
	@Override
	public String getServletInfo() {
		return "ACARS Track Servlet " + VersionInfo.TXT_COPYRIGHT;
	}
	
	private static TileAddress getTileAddress(String uri) {
		
		// Parse the URL and get the path parts
		URLParser url = new URLParser(uri);
		LinkedList<String> pathParts = url.getPath();
		Collections.reverse(pathParts);
		
		// Pop Z/Y/Z
		try {
			int z = Integer.parseInt(pathParts.pop());
			int y = Integer.parseInt(pathParts.pop());
			int x = Integer.parseInt(pathParts.pop());
			return new TileAddress(x, y, z);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Processes HTTP GET requests for images.
	 * @param req the HTTP request
	 * @param rsp the HTTP response
	 * @throws IOException if a network I/O error occurs
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
		
		// Parse the URL and get the tile address
		TileAddress addr = getTileAddress(req.getRequestURI());
		if (addr == null) {
			rsp.sendError(SC_BAD_REQUEST);
			return;
		}
		
		byte[] data = EMPTY;
		if (addr.getLevel() < 14) {
		
			// Get the connection pool
			ConnectionPool<Connection> pool = SystemData.getJDBCPool();
			Connection c = null; 
			try {
				c = pool.getConnection();
				GetImage dao = new GetImage(c);
				data = dao.getTile(addr);
				if (data == null)
					data = EMPTY;
			} catch (ConnectionPoolException cpe) {
				log.error(cpe.getMessage());
			} catch (ControllerException ce) {
				if (ce.isWarning())
					log.warn("Error retrieving image - {}", ce.getMessage());
				else
					log.error("Error retrieving image - {}", ce.getMessage(), ce.getLogStackDump() ? ce : null);
			} finally {
				pool.release(c);
			}
		}

		writeTile(rsp, data);
	}
}