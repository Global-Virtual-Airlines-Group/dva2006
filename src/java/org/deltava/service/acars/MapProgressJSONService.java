// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2016, 2017, 2018, 2022, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.FlightInfo;
import org.deltava.beans.flight.Recorder;
import org.deltava.beans.navdata.NavigationDataBean;

import org.deltava.dao.*;
import org.deltava.dao.jedis.GetTrack;

import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to provide JSON-formatted ACARS progress data for Google Maps.
 * @author Luke
 * @version 12.0
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
		String id = ctx.getParameter("id"); boolean isExternal = Boolean.parseBoolean(ctx.getParameter("isExternal"));
		int acarsID = StringUtils.parse(id, 0);
		if ((acarsID < 1) && !isExternal) 
			return SC_NOT_FOUND;

		// Determine if we show the route
		boolean doRoute = Boolean.parseBoolean(ctx.getParameter("route"));

		// Get the DAO and the route data
		final List<GeoLocation> pathPoints = new ArrayList<GeoLocation>();
		final List<GeoLocation> tempPoints = new ArrayList<GeoLocation>();
		final Collection<MarkerMapEntry> routeWaypoints = new ArrayList<MarkerMapEntry>();
		
		try {
			Connection con = ctx.getConnection();
			GetACARSPositions dao = new GetACARSPositions(con); FlightInfo info = null; GetTrack tkdao = new GetTrack();
			if (!isExternal) {
				info = dao.getInfo(acarsID);
				if ((info != null) && (info.getFDR() == Recorder.XACARS))
					pathPoints.addAll(dao.getXACARSEntries(acarsID));
				else if (info != null)
					pathPoints.addAll(dao.getRouteEntries(acarsID, false, false));
			} else
				tempPoints.addAll(tkdao.getTrack(false, id));
			
			// Get temporary waypoints
			tempPoints.addAll(tkdao.getTrack(true, String.valueOf(id)));
			if (!pathPoints.isEmpty())
				tempPoints.add(0, pathPoints.getLast());

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
		
		// Plot GC route for MapBox
		final List<GeoLocation> routePathPoints = GeoUtils.greatCircle(routeWaypoints.stream().map(GeoLocation.class::cast).collect(Collectors.toList()));
		
		// Handle cross-IDL lines for MapBox
		GeoUtils.translate(pathPoints);
		GeoUtils.translate(tempPoints);
		GeoUtils.translate(routePathPoints);
		
		// Generate the JSON document
		JSONObject jo = new JSONObject();
		jo.put("id", id);
		pathPoints.forEach(e -> jo.append("savedPositions", JSONUtils.format(e)));
		tempPoints.forEach(e -> jo.append("tempPositions", JSONUtils.format(e)));
		routePathPoints.forEach(e -> jo.append("routePathPoints",  JSONUtils.format(e)));
		
		// Write the route
		for (MapEntry entry : routeWaypoints) {
			JSONObject eo = new JSONObject();
			eo.put("ll", JSONUtils.format(entry));
			if (entry instanceof NavigationDataBean nd)
				eo.put("code", nd.getCode());
			
			eo.put("route", entry.getInfoBox());
			if (entry instanceof IconMapEntry ime) {
				eo.put("pal", ime.getPaletteCode());
				eo.put("icon", ime.getIconCode());
			} else if (entry instanceof MarkerMapEntry mme)
				eo.put("color", mme.getIconColor());
			
			jo.append("waypoints", eo);
		}
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "savedPositions", "tempPositions", "waypoints", "routePathPoints");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(5);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	@Override
	public final boolean isLogged() {
		return false;
	}
}