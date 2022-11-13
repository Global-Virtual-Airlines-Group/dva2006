// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.Recorder;
import org.deltava.beans.navdata.*;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display ACARS Flight Report data.
 * @author Luke
 * @version 10.3
 * @since 1.0
 */

public class MapFlightDataService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
   public int execute(ServiceContext ctx) throws ServiceException {
      
		// Get the DAO and the route data
		int id = StringUtils.parse(ctx.getParameter("id"), 0);
		Collection<? extends GeospaceLocation> routePoints = null; FlightInfo info = null;
		Collection<Airspace> airspaces = new LinkedHashSet<Airspace>();
		try {
			Connection con = ctx.getConnection();
			
			// Load flight data
			GetACARSPositions dao = new GetACARSPositions(con);
			info = dao.getInfo(id);
			if (info == null)
				throw error(SC_NOT_FOUND, "Invalid ACARS Flight ID", false);
			if ((info.getFDR() == Recorder.XACARS) && !info.getArchived())
				routePoints = dao.getXACARSEntries(id);
			else
				routePoints = dao.getRouteEntries(id, false, info.getArchived());
				
			// Check airspace
			for (GeospaceLocation rt : routePoints) {
				ACARSRouteEntry re = (rt instanceof ACARSRouteEntry) ? (ACARSRouteEntry) rt : null;
				if (re != null)
					re.setAutopilotType(info.getAutopilotType());
				
				Airspace a = Airspace.isRestricted(rt);
				if (a != null)
					airspaces.add(a);
				if (rt.getAltitude() > 18000) {
					if (re != null)
						re.setAirspace((a == null) ? AirspaceType.fromAltitude(re.getRadarAltitude(), re.getAltitude()) : a.getType());
				}
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Check airspace
		GeospaceLocation lastLoc = null;
		for (GeospaceLocation rt : routePoints) {
			ACARSRouteEntry re = (rt instanceof ACARSRouteEntry) ? (ACARSRouteEntry) rt : null;
			if (re != null)
				re.setAutopilotType(info.getAutopilotType());
			
			Airspace a = Airspace.isRestricted(rt);
			if (a != null)
				airspaces.add(a);
			if ((rt.getAltitude() > 18000) && (re != null))
				re.setAirspace((a == null) ? AirspaceType.fromAltitude(re.getRadarAltitude(), re.getAltitude()) : a.getType());
			else if (rt.distanceFeet(lastLoc) > 52800) {
				airspaces.addAll(Airspace.findRestricted(rt, 10));
				if (re != null)
					re.setAirspace((a == null) ? AirspaceType.fromAltitude(re.getRadarAltitude(), re.getAltitude()) : a.getType());
			}
			
			lastLoc = rt;
		}
		
		// Write the positions - Gracefully handle geopositions - don't append a color and let the JS handle this
		JSONObject jo = new JSONObject();
		jo.put("id", id);
		for (GeoLocation entry : routePoints) {
			JSONObject eo = new JSONObject(); 
			eo.put("ll", JSONUtils.format(entry));
			
			if (entry instanceof MarkerMapEntry) {
				MarkerMapEntry me = (MarkerMapEntry) entry;
				eo.put("color", me.getIconColor());
				eo.put("info", me.getInfoBox());
			} else if (entry instanceof IconMapEntry) {
				IconMapEntry me = (IconMapEntry) entry;
				eo.put("pal", me.getPaletteCode());
				eo.put("icon", me.getIconCode());
				eo.put("info", me.getInfoBox());
			} 
			
			if (entry instanceof ACARSRouteEntry) {
				ACARSRouteEntry rte = (ACARSRouteEntry) entry;
				if (rte.getATC1() != null) {
					JSONObject ao = new JSONObject();
					Controller ctr = rte.getATC1();
					ao.put("id", ctr.getCallsign());
					ao.put("type", String.valueOf(ctr.getFacility()));
					if ((ctr.getFacility() != Facility.CTR) && (ctr.getFacility() != Facility.FSS)) {
						ao.put("ll", JSONUtils.format(ctr));
						ao.put("range", ctr.getFacility().getRange());
					}
					
					eo.put("atc", ao);
				}
			}
			
			jo.append("positions", eo);
		}
		
		// Load airspace boundaries
		for (Airspace a : airspaces) {
			JSONObject ao = new JSONObject();
			ao.put("id", a.getID());
			ao.put("type", a.getType().name());
			ao.put("min", a.getMinAltitude());
			ao.put("max", a.getMaxAltitude());
			ao.put("exclude", a.isExclusion());
			ao.put("info", a.getInfoBox());
			ao.put("ll", JSONUtils.format(a));
			a.getBorder().forEach(pt -> ao.append("border", JSONUtils.format(pt)));
			JSONUtils.ensureArrayPresent(ao, "border");
			jo.append("airspace", ao);
		}
		
		// Write departure/arrival runway disatnces
		jo.putOpt("runwayD", formatRunway(info.getRunwayD()));
		jo.putOpt("runwayA", formatRunway(info.getRunwayA()));
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "positions", "airspace");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(3600);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
   }
	
	/*
	 * Helper method to format runway distance objects.
	 */
	private static JSONObject formatRunway(Runway r) {
		if (!(r instanceof RunwayDistance)) return null;
		RunwayDistance rd = (RunwayDistance) r;
		double distanceMiles = rd.getDistance() * 1.0d / GeoLocation.FEET_MILES;
		JSONObject ro = new JSONObject();
		ro.put("name", r.getName());
		ro.put("location", JSONUtils.format(rd));
		ro.put("distance", rd.getDistance());
		ro.put("hdg", rd.getHeading());		
		ro.put("pt", JSONUtils.format(GeoUtils.bearingPointS(rd, distanceMiles, rd.getHeading() - rd.getMagVar())));
		ro.put("thresholdLength", rd.getThresholdLength());
		if (rd.getThresholdLength() > 0)
			ro.put("threshold", JSONUtils.format(rd.getThreshold()));
		
		return ro;
	}

	@Override
	public final boolean isLogged() {
		return false;
	}
}