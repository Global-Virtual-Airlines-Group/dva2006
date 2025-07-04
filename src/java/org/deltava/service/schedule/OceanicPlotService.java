// Copyright 2007, 2008, 2009, 2010, 2012, 2016, 2017, 2020, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.time.Instant;
import java.io.IOException;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.OceanicNOTAM;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to return Oceanic Track data.
 * @author Luke
 * @version 12.0
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
		OceanicTrackInfo.Type trackType = EnumUtils.parse(OceanicTrackInfo.Type.class, ctx.getParameter("type"), OceanicTrackInfo.Type.NAT); 
		DailyOceanicTracks tracks = null; OceanicNOTAM nt = null;
		try {
			GetOceanicRoute dao = new GetOceanicRoute(ctx.getConnection());
			nt = dao.get(trackType, dt);
			tracks = dao.getOceanicTracks(trackType, dt);
			if (trackType == OceanicTrackInfo.Type.NAT)
				dao.loadConcordeNATs().forEach(tracks::addTrack);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Generate the JSON document
		JSONObject jo = new JSONObject();
		jo.put("timestamp", tracks.getDate().toEpochMilli());
		jo.put("date", StringUtils.format(tracks.getDate(), ctx.isAuthenticated() ? ctx.getUser().getDateFormat() : "MM/dd/yyyy"));
		if ((nt != null) && (nt.getFetchDate() != null)) {
			jo.put("fetchTime", nt.getFetchDate().toEpochMilli());
			jo.put("fetchDate", StringUtils.format(nt.getFetchDate(), "MM/dd/yyyy HH:mm"));
			jo.put("src", nt.getSource());
		}

		// Build the track data
		for (OceanicTrack ow : tracks.getTracks()) {
			boolean isEast = (ow.getDirection() == OceanicTrackInfo.Direction.EAST);
			JSONObject to = new JSONObject();
			to.put("code", ow.getTrack());
			to.put("type", ow.isFixed() ? "C" : (isEast ? "E" : "W"));
			to.put("color", ow.isFixed() ? "#2040e0" : (isEast ? "#eeeeee" : "#eeee44"));
			to.put("track", ow.getRoute());
			
			// Plot the waypoints
			for (NavigationDataBean ndb : ow.getWaypoints()) {
				JSONObject wo = new JSONObject();
				wo.put("code", ndb.getCode());
				wo.put("ll", JSONUtils.format(ndb));
				wo.put("pal", ndb.getPaletteCode());
				wo.put("icon", ndb.getIconCode());
				wo.put("color", ow.isFixed() ? MapEntry.BLUE : (isEast ? MapEntry.WHITE : MapEntry.ORANGE));
				wo.put("info", ndb.getInfoBox());
				to.append("waypoints", wo);
			}

			List<GeoLocation> gcPts = GeoUtils.greatCircle(ow.getWaypoints());
			gcPts.forEach(pt -> to.append("pts", JSONUtils.toLL(pt)));
			JSONUtils.ensureArrayPresent(to, "waypoints", "pts");
			jo.append("tracks", to);
		}

		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "tracks");
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

	@Override
	public boolean isLogged() {
		return false;
	}

	@Override
	public boolean isSecure() {
		return true;
	}
}