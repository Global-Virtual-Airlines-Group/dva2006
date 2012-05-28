// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.io.*;
import java.sql.Connection;

import javax.imageio.ImageIO;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.system.VersionInfo;

import org.deltava.dao.GetImage;

import org.deltava.util.URLParser;
import org.deltava.util.tile.*;

import org.gvagroup.jdbc.*;

/**
 * A servlet to display ACARS track tiles.
 * @author Luke
 * @version 4.2
 * @since 4.2
 */

public class TileServlet extends GenericServlet {

	private static final Logger log = Logger.getLogger(TileServlet.class);
	
	private byte[] EMPTY;
	
	/**
	 * Returns the servlet description.
	 * @return name, author and copyright info for this servlet
	 */
	@Override
	public String getServletInfo() {
		return "ACARS Track Image Servlet " + VersionInfo.TXT_COPYRIGHT;
	}
	
	/**
	 * Initializes the servlet.
	 */
	@Override
	public void init() {

		SingleTile st = new SingleTile(new TileAddress(0, 0, 1));
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
			ImageIO.write(st.getImage(), "png", out);
			EMPTY = out.toByteArray();
		} catch (IOException ie) {
			// NOOP
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
		URLParser url = new URLParser(req.getRequestURI());
		TileAddress addr = new TileAddress(url.getName());
		byte[] data = EMPTY;
		if (addr.getLevel() < 13) {
		
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

		// Set headers
		rsp.setHeader("Cache-Control", "public");
		rsp.setIntHeader("max-age", 3600);
		rsp.setContentType("application/octet-stream");
		rsp.setStatus(HttpServletResponse.SC_OK);
		rsp.setContentLength(data.length);
		rsp.setBufferSize(Math.min(65536, data.length + 16));

		// Dump the data to the output stream
		try {
			rsp.getOutputStream().write(data);
			rsp.flushBuffer();
		} catch (IOException ie) {
			// NOOP
		}
	}
}