// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.servinfo;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom2.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.navdata.TerminalRoute;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.*;
import org.deltava.dao.file.GetServInfo;

import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to download ServInfo route data for Google Maps.
 * @author Luke
 * @version 5.1
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

		// Get the network
		OnlineNetwork net = OnlineNetwork.valueOf(networkName.toUpperCase());

		// Get the network info from the cache
		NetworkInfo info = null;
		try {
			File f = new File(SystemData.get("online." + net.toString().toLowerCase() + ".local.info"));
			GetServInfo sidao = new GetServInfo(new FileInputStream(f));
			info = sidao.getInfo(net);
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}

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
				int trackID = otdao.getTrackID(p.getPilotID(), net, new Date(), p.getAirportD(), p.getAirportA());
				if (trackID != 0)
					trackInfo.addAll(otdao.getRaw(trackID));
			}

			// Populate the route if required
			if (!p.isPopulated()) {
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

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Render the route
		for (GeoLocation loc : p.getWaypoints()) {
			Element e = new Element("waypoint");
			e.setAttribute("lat", StringUtils.format(loc.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(loc.getLongitude(), "##0.00000"));
			re.addContent(e);
		}
		
		// Render the track
		for (GeoLocation loc : trackInfo) {
			Element e = new Element("track");
			e.setAttribute("lat", StringUtils.format(loc.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(loc.getLongitude(), "##0.00000"));
			re.addContent(e);
		}

		// Dump the XML to the output stream
		try {
			ctx.setContentType("text/xml", "UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	public final boolean isLogged() {
		return false;
	}
}