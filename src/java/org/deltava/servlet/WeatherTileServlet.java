// Copyright 2012, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.io.IOException;

import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.system.VersionInfo;

import org.deltava.dao.DAOException;
import org.deltava.dao.redis.GetTiles;
import org.gvagroup.tile.PNGTile;

/**
 * A servlet to fetch weather quadtree tiles.
 * @author Luke
 * @version 7.0
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
		TileAddress5D addr = getTileAddress(req.getRequestURI(), true);
		
		// Get the data
		byte[] data = EMPTY;
		try {
			GetTiles trdao = new GetTiles();
			PNGTile pt = trdao.getTile(addr.getType(), addr.getDate(), addr);
			if (pt != null)
				data = pt.getData();
		} catch (DAOException e) {
			log.error("Error fetching " + addr, e);
		}
		
		writeTile(rsp, data);
	}
}