// Copyright 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.sql.Connection;

import org.json.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.StringUtils;

/**
 * A Web Service to display flight data in JSON format. 
 * @author Luke
 * @version 7.0
 * @since 5.0
 */

public class FlightStatsService extends WebService {
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		Collection<? extends GeoLocation> routePoints = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the Flight Report
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport fr = frdao.get(StringUtils.parse(ctx.getParameter("id"), 0));
			if (fr == null)
				throw error(SC_NOT_FOUND, "Invalid Flight Report", false);
			
			// Get the position data
			GetACARSPositions dao = new GetACARSPositions(con);
			FlightInfo info = dao.getInfo(fr.getDatabaseID(DatabaseID.ACARS));
			if (info == null)
				routePoints = Collections.emptyList();
			else if ((info.getFDR() == Recorder.XACARS) && !info.getArchived())
				routePoints = dao.getXACARSEntries(info.getID());
			else
				routePoints = dao.getRouteEntries(info.getID(), true, info.getArchived());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Go through the rows
		JSONObject jo = new JSONObject();
		int maxSpeed = 0; int maxAlt = 0;
		for (GeoLocation loc : routePoints) {
			if (loc instanceof RouteEntry) {
				RouteEntry re = (RouteEntry) loc;
				JSONArray je = new JSONArray();
				je.put(re.getDate().toEpochMilli());
				je.put(re.getGroundSpeed());
				je.put(re.getAltitude());
				if (re instanceof ACARSRouteEntry) {
					ACARSRouteEntry ae = (ACARSRouteEntry) re;
					je.put(Math.max(0, ae.getAltitude() - ae.getRadarAltitude()));
				}
					
				jo.append("data", je);
				maxSpeed = Math.max(maxSpeed, re.getGroundSpeed());
				maxAlt = Math.max(maxAlt, re.getAltitude());
			}
		}
		
		// Adjust maximum speed and altitude
		maxAlt = (maxAlt + 10000 - (maxAlt % 10000));
		maxSpeed = (maxSpeed + 100 - (maxSpeed % 100));
		jo.put("maxAlt", maxAlt);
		jo.put("maxSpeed", maxSpeed);
		
		// Dump to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(3600);
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