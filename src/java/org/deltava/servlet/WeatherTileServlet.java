// Copyright 2012, 2013, 2016, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import java.io.IOException;
import javax.servlet.http.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.system.VersionInfo;

import org.deltava.dao.DAOException;
import org.deltava.dao.jedis.GetTiles;
import org.deltava.util.tile.PNGTile;

/**
 * A servlet to fetch weather quadtree tiles.
 * @author Luke
 * @version 11.5
 * @since 5.0
 */

public class WeatherTileServlet extends TileServlet {

	private static final Logger log = LogManager.getLogger(WeatherTileServlet.class);
	
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
		TileAddress5D addr = getTileAddress(req.getRequestURI(), true);
		if (addr == null) {
			rsp.sendError(SC_BAD_REQUEST);
			return;
		}
		
		// Get the data
		byte[] data = EMPTY;
		try {
			GetTiles trdao = new GetTiles();
			PNGTile pt = trdao.getTile(addr.getType(), addr.getDate(), addr);
			if (pt != null)
				data = pt.getData();
		} catch (DAOException e) {
			log.atError().withThrowable(e).log("Error fetching {}", addr);
		}
		
		writeTile(rsp, data);
	}
}