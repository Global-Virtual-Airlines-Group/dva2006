// Copyright 2007, 2008, 2009, 2010, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.time.Instant;
import java.io.IOException;

import org.json.*;

import org.deltava.beans.MapEntry;
import org.deltava.beans.navdata.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to return Oceanic Track data.
 * @author Luke
 * @version 7.3
 * @since 1.0
 */

public class OceanicPlotService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the date
		Instant dt = null;
		try {
			dt = StringUtils.parseInstant(ctx.getParameter("date"), ctx.getUser().getDateFormat());
		} catch (Exception e) {
			// empty
		}
		
		// Get the track type
		OceanicTrackInfo.Type trackType = OceanicTrackInfo.Type.NAT;
		try {
			trackType = OceanicTrackInfo.Type.valueOf(ctx.getParameter("type").toUpperCase());
		} catch (Exception e) {
			// empty
		}

		DailyOceanicTracks tracks = null;
		try {
			GetOceanicRoute dao = new GetOceanicRoute(ctx.getConnection());
			tracks = dao.getOceanicTracks(trackType, dt);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Add concorde routes if NAT
		if (trackType == OceanicTrackInfo.Type.NAT)
			tracks.addAll(OceanicTrack.CONC_ROUTES);

		// Generate the JSON document
		JSONObject jo = new JSONObject();
		jo.put("date", StringUtils.format(tracks.getDate(), "MM/dd/yyyy"));

		// Build the track data
		for (OceanicTrack ow : tracks.getTracks()) {
			boolean isEast = (ow.getDirection() == OceanicTrackInfo.Direction.EAST);
			JSONObject to = new JSONObject();
			to.put("code", ow.getTrack());
			to.put("type", ow.isFixed() ? "C" : (isEast ? "E" : "W"));
			to.put("color", ow.isFixed() ? "#2040E0" : (isEast ? "#EEEEEE" : "#EEEE44"));
			to.put("track", ow.getRoute());
			for (NavigationDataBean ndb : ow.getWaypoints()) {
				JSONObject wo = new JSONObject();
				wo.put("code", ndb.getCode());
				wo.put("ll", GeoUtils.toJSON(ndb));
				wo.put("color", ow.isFixed() ? MapEntry.BLUE : (isEast ? MapEntry.WHITE : MapEntry.ORANGE));
				wo.put("info", ndb.getInfoBox());
				to.accumulate("waypoints", wo);
			}

			jo.accumulate("tracks", to);
		}

		// Dump the XML to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(3600);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	/**
	 * Returns if the Web Service invocation is logged.
	 * @return FALSE
	 */
	@Override
	public boolean isLogged() {
		return false;
	}

	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE
	 */
	@Override
	public boolean isSecure() {
		return true;
	}
}