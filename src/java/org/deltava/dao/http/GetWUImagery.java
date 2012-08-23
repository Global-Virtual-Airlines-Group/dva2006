// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import javax.imageio.ImageIO;

import org.deltava.beans.GeoLocation;

import org.deltava.dao.DAOException;

import org.deltava.util.tile.*;

/**
 * A Data Access Object for Weather Underground radar imagery. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class GetWUImagery extends DAO {

	/**
	 * Loads a Weather Underground radar composite as a SuperTile.
	 * @param loc the GeoLocation of the top-left corner
	 * @param w the width of the supertile in pixels
	 * @param h the height of the supertile in pixels
	 * @param zoom the zoom level
	 * @return a SuperTile object
	 * @throws DAOException if an I/O error occurs
	 */
	public SuperTile getRadar(GeoLocation loc, int w, int h, int zoom) throws DAOException {
		
		// Calculate the coordinates of the NW and SE corners
		Projection p = new MercatorProjection(zoom);
		TileAddress addr = p.getAddress(loc);
		GeoLocation nw = p.getGeoPosition(addr.getPixelX(), addr.getPixelY());
		GeoLocation se = p.getGeoPosition(addr.getPixelX() + w, addr.getPixelY() + h);

		// Build the URL
		StringBuilder urlBuf = new StringBuilder("http://radblast-mi.wunderground.com/cgi-bin/radar/WUNIDS_composite?maxlat=");
		urlBuf.append(nw.getLatitude());
		urlBuf.append("&minlat=");
		urlBuf.append(se.getLatitude());
		urlBuf.append("&maxlon=");
		urlBuf.append(se.getLongitude());
		urlBuf.append("&minlon=");
		urlBuf.append(nw.getLongitude());
		urlBuf.append("&width=");
		urlBuf.append(w);
		urlBuf.append("&height=");
		urlBuf.append(h);
		urlBuf.append("&type=N0R&frame=0&smooth=1&rainsnow=1&reproj.automerc=1&png=1");
		
		try {
			init(urlBuf.toString());
			SuperTile st = new SuperTile(addr);
			try (InputStream is = getIn()) {
				st.setImage(ImageIO.read(is));
				return st;
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}