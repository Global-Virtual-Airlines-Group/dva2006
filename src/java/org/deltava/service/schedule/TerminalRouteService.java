// Copyright 2008, 2012, 2013, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.json.*;

import org.deltava.beans.navdata.TerminalRoute;
import org.deltava.beans.schedule.Airport;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Service to display the SIDs and STARs for a particular Airport pair.
 * @author Luke
 * @version 6.0
 * @since 2.2
 */

public class TerminalRouteService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Load Airports
		Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
		Airport aA = SystemData.getAirport(ctx.getParameter("airportA"));
		
		List<TerminalRoute> tRoutes = new ArrayList<TerminalRoute>();
		try {
			GetNavRoute dao = new GetNavRoute(ctx.getConnection());
			if (aD != null)
				tRoutes.addAll(new TreeSet<TerminalRoute>(dao.getRoutes(aD, TerminalRoute.Type.SID)));
			if (aA != null)
				tRoutes.addAll(new TreeSet<TerminalRoute>(dao.getRoutes(aA, TerminalRoute.Type.STAR)));			
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Add SID/STARs to JSON document
		try {
			JSONObject ro = new JSONObject();
			JSONArray sids = new JSONArray(); JSONArray stars = new JSONArray();
			for (TerminalRoute tr : tRoutes) {
				JSONObject jo = new JSONObject();
				jo.put("name", tr.getName());
				jo.put("transition", tr.getTransition());
				jo.put("code", tr.getCode());
				JSONArray dst = (tr.getType() == TerminalRoute.Type.SID) ? sids : stars; 
				dst.put(jo);
			}
		
			// Dump the JSON to the output stream
			ro.put("sid", sids); ro.put("star", stars);
			ctx.setContentType("text/javascript", "UTF-8");
			ctx.println(ro.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error");
		}
		
		return SC_OK;
	}
	
	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	@Override
	public boolean isLogged() {
		return false;
	}
}