// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2016, 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.FlightInfo;
import org.deltava.beans.flight.Recorder;

import org.deltava.dao.*;
import org.deltava.dao.redis.GetTrack;

import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to provide JSON-formatted ACARS progress data for Google Maps.
 * @author Luke
 * @version 8.3
 * @since 7.3
 */

public class MapProgressJSONService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the Flight ID
		String id = ctx.getParameter("id"); boolean isExternal = Boolean.valueOf(ctx.getParameter("isExternal")).booleanValue();
		int acarsID = StringUtils.parse(id, 0);
		if ((acarsID < 1) && !isExternal) 
			return SC_NOT_FOUND;

		// Determine if we show the route
		boolean doRoute = Boolean.valueOf(ctx.getParameter("route")).booleanValue();

		// Get the DAO and the route data
		final List<GeoLocation> routePoints = new ArrayList<GeoLocation>();
		final Collection<MarkerMapEntry> routeWaypoints = new ArrayList<MarkerMapEntry>();
		final List<GeoLocation> tempPoints = new ArrayList<GeoLocation>();
		try {
			Connection con = ctx.getConnection();
			GetACARSPositions dao = new GetACARSPositions(con); FlightInfo info = null; GetTrack tkdao = new GetTrack();
			if (!isExternal) {
				info = dao.getInfo(acarsID);
				if ((info != null) && (info.getFDR() == Recorder.XACARS))
					routePoints.addAll(dao.getXACARSEntries(acarsID));
				else if (info != null)
					routePoints.addAll(dao.getRouteEntries(acarsID, false, false));
			} else
				tempPoints.addAll(tkdao.getTrack(false, id));
			
			// Get temporary waypoints
			tempPoints.addAll(tkdao.getTrack(true, String.valueOf(id)));
			if (!routePoints.isEmpty())
				tempPoints.add(0, routePoints.get(routePoints.size() - 1));

			// Load the route and the route waypoints
			if ((info != null) && doRoute) {
				Collection<MarkerMapEntry> wps = new LinkedHashSet<MarkerMapEntry>(); 
				GetNavRoute navdao = new GetNavRoute(con);
				wps.add(info.getAirportD());
				if (info.getRunwayD() != null)
					wps.add(info.getRunwayD());
				if (info.getSID() != null)
					wps.addAll(info.getSID().getWaypoints());
				wps.addAll(navdao.getRouteWaypoints(info.getRoute(), info.getAirportD()));
				if (info.getSTAR() != null)
					wps.addAll(info.getSTAR().getWaypoints());
				if (info.getRunwayA() != null)
					wps.add(info.getRunwayA());
				wps.add(info.getAirportA());
				
				// Trim spurious entries
				routeWaypoints.addAll(GeoUtils.stripDetours(wps, 150));
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Generate the JSON document
		JSONObject jo = new JSONObject();
		jo.put("id", id);
		routePoints.forEach(entry -> jo.append("savedPositions", JSONUtils.format(entry)));
		tempPoints.forEach(entry -> jo.append("tempPositions", JSONUtils.format(entry)));
		
		// Write the route
		for (MapEntry entry : routeWaypoints) {
			JSONObject eo = new JSONObject();
			eo.put("ll", JSONUtils.format(entry));
			eo.put("route", entry.getInfoBox());
			if (entry instanceof IconMapEntry) {
				IconMapEntry ime = (IconMapEntry) entry;
				eo.put("pal", ime.getPaletteCode());
				eo.put("icon", ime.getIconCode());
			} else
				eo.put("color", ((MarkerMapEntry) entry).getIconColor());
			
			jo.append("waypoints", eo);
		}
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "savedPositions", "tempPositions", "waypoints");
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(5);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	@Override
	public final boolean isLogged() {
		return false;
	}
}