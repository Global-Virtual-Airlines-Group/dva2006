// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2012, 2014, 2016, 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.servinfo;

import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;
import org.deltava.beans.GeoLocation;
import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.navdata.*;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.*;

import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to download ServInfo route data for Google Maps.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class MapRouteService extends WebService {

	/**
	 * Executes the Web Service, returning ServInfo route data.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the network name
		String networkName = ctx.getParameter("network");
		if (networkName == null)
			networkName = SystemData.get("online.default_network");

		// Get the network info
		OnlineNetwork net = OnlineNetwork.valueOf(networkName.toUpperCase());
		NetworkInfo info = ServInfoHelper.getInfo(net);

		// Get the Pilot
		NetworkUser usr = info.get(StringUtils.parse(ctx.getParameter("id"), 0));
		if (usr == null)
			throw error(SC_NOT_FOUND, "Cannot find " + ctx.getParameter("id"), false);
		else if (usr.getType() != NetworkUser.Type.PILOT)
			throw error(SC_NOT_FOUND, usr.getName() + " is not a Pilot", false);

		Pilot p = (Pilot) usr;
		Collection<PositionData> trackInfo = new ArrayList<PositionData>();
		try {
			Connection con = ctx.getConnection();
			
			// Load Track data
			if (!info.hasPilotIDs()) {
				GetPilotOnline podao = new GetPilotOnline(con);
				info.setPilotIDs(podao.getIDs(net));
				p = info.getPilot(p.getCallsign());
			}
			
			// Load the online track
			if (p.getPilotID() != 0) {
				GetOnlineTrack otdao = new GetOnlineTrack(con);
				int trackID = otdao.getTrackID(p.getPilotID(), net, Instant.now(), p.getAirportD(), p.getAirportA());
				if (trackID != 0)
					trackInfo.addAll(otdao.getRaw(trackID));
			}

			// Populate the route if required
			if (!p.isRoutePopulated()) {
				GetNavRoute navdao = new GetNavRoute(con);

				// Split the route
				List<String> wps = StringUtils.nullTrim(StringUtils.split(p.getRoute(), " "));
				wps.remove(p.getAirportD().getICAO());
				wps.remove(p.getAirportA().getICAO());

				// Load the SID
				if (wps.size() > 2) {
					String name = wps.get(0);
					TerminalRoute sid = navdao.getBestRoute(p.getAirportD(), TerminalRoute.Type.SID, TerminalRoute.makeGeneric(name), wps.get(1), (String) null);
					if (sid != null) {
						wps.remove(0);
						if (!CollectionUtils.isEmpty(wps))
							p.addWaypoints(sid.getWaypoints(wps.get(0)));
						else
							p.addWaypoints(sid.getWaypoints());
					}
				}

				p.addWaypoints(navdao.getRouteWaypoints(StringUtils.listConcat(wps, " "), p.getAirportD()));

				// Load the STAR
				if (wps.size() > 2) {
					String name = wps.get(wps.size() - 1);
					TerminalRoute star = navdao.getBestRoute(p.getAirportA(), TerminalRoute.Type.STAR, TerminalRoute.makeGeneric(name), wps.get(wps.size() - 2), (String) null);
					if (star != null) {
						wps.remove(wps.size() - 1);
						if (!CollectionUtils.isEmpty(wps))
							p.addWaypoints(star.getWaypoints(wps.get(wps.size() - 1)));
						else
							p.addWaypoints(star.getWaypoints());
					}
				}
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Generate the JSON document
		JSONObject jo = new JSONObject();
		trackInfo.forEach(loc -> jo.append("track", JSONUtils.format(loc)));
		for (GeoLocation loc : p.getWaypoints()) {
			jo.append("route", JSONUtils.format(loc));
			if (!(loc instanceof NavigationDataBean)) continue;
			NavigationDataBean ndb = (NavigationDataBean) loc;
			JSONObject lo = new JSONObject();
			lo.put("ll", JSONUtils.format(loc));
			lo.put("code", ndb.getCode());
			lo.put("pal", ndb.getPaletteCode());
			lo.put("icon", ndb.getIconCode());
			lo.put("color", ndb.getIconColor());
			lo.put("info", ndb.getInfoBox());
			jo.append("waypoints", lo);
		}

		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(120);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	@Override
	public final boolean isLogged() {
		return false;
	}
}