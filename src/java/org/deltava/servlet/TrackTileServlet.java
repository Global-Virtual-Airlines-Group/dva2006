// Copyright 2012, 2013, 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.io.*;
import java.sql.Connection;

import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.system.VersionInfo;

import org.deltava.dao.GetImage;

import org.deltava.util.ControllerException;
import org.deltava.util.tile.*;

import org.gvagroup.jdbc.*;

/**
 * A servlet to display ACARS track tiles.
 * @author Luke
 * @version 5.4
 * @since 5.0
 */

public class TrackTileServlet extends TileServlet {

	private static final Logger log = Logger.getLogger(TrackTileServlet.class);
	
	/**
	 * Returns the servlet description.
	 * @return name, author and copyright info for this servlet
	 */
	@Override
	public String getServletInfo() {
		return "ACARS Track Image Servlet " + VersionInfo.TXT_COPYRIGHT;
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
		TileAddress addr = getTileAddress(req.getRequestURI(), false);
		byte[] data = EMPTY;
		if (addr.getLevel() < 14) {
		
			// Get the connection pool
			ConnectionPool jdbcPool = getConnectionPool();
			Connection c = null; 
			try {
				c = jdbcPool.getConnection();
				GetImage dao = new GetImage(c);
				data = dao.getTile(addr.getX(), addr.getY(), addr.getLevel());
				if (data == null)
					data = EMPTY;
			} catch (ConnectionPoolException cpe) {
				log.error(cpe.getMessage());
			} catch (ControllerException ce) {
				if (ce.isWarning())
					log.warn("Error retrieving image - " + ce.getMessage());
				else
					log.error("Error retrieving image - " + ce.getMessage(), ce.getLogStackDump() ? ce : null);
			} finally {
				jdbcPool.release(c);
			}
		}

		writeTile(rsp, data);
	}
}