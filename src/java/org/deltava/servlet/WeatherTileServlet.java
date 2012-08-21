// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.io.IOException;

import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.system.VersionInfo;

import org.deltava.dao.DAOException;
import org.deltava.dao.mc.GetTiles;

import org.deltava.util.tile.PNGTile;

/**
 * A servlet to fetch weather quadtree tiles.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class WeatherTileServlet extends TileServlet {

	private static final Logger log = Logger.getLogger(WeatherTileServlet.class);
	
	/**
	 * Returns the servlet description.
	 * @return name, author and copyright info for this servlet
	 */
	@Override
	public String getServletInfo() {
		return "Weather Image Servlet " + VersionInfo.TXT_COPYRIGHT;
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
		TileAddress5D addr = getTileAddress(req.getRequestURI());
		byte[] data = EMPTY;
		
		// Get the data
		GetTiles trdao = new GetTiles();
		try {
			PNGTile pt = trdao.getTile(addr.getType(), addr.getDate(), addr);
			if (pt != null)
				data = pt.getData();
		} catch (DAOException e) {
			log.error("Error fetching " + addr, e);
		}

		// Set headers
		rsp.setHeader("Cache-Control", "public");
		rsp.setIntHeader("max-age", 300);
		rsp.setContentType("image/png");
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