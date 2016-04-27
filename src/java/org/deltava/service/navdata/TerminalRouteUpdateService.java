// Copyright 2008, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.util.*;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Service to handle AJAX updates of Terminal Routes.
 * @author Luke
 * @version 7.0
 * @since 2.1
 */

public class TerminalRouteUpdateService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Validate access
		if (!ctx.isUserInRole("Schedule"))
			throw error(SC_UNAUTHORIZED, "Not in Schedule role", false);
		
		// Get the airport
		Airport a = SystemData.getAirport(ctx.getParameter("icao"));
		if (a == null)
			throw error(SC_NOT_FOUND, "Unknown Airport - " + ctx.getParameter("icao"), false);
		
		// Get the type
		TerminalRoute.Type rt = TerminalRoute.Type.valueOf(ctx.getParameter("type").toUpperCase());
		
		// Build the Terminal Route
		TerminalRoute tr = new TerminalRoute(a.getICAO(), ctx.getParameter("name"), rt);
		tr.setTransition(ctx.getParameter("transition"));
		tr.setRunway(ctx.getParameter("runway"));
		tr.setCanPurge(Boolean.valueOf(ctx.getParameter("canPurge")).booleanValue());
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the existing SID
			GetNavRoute dao = new GetNavRoute(con);
			TerminalRoute otr = dao.getRoute(a, rt, tr.getName());
			
			// Get the transition
			NavigationDataBean tx = dao.get(tr.getTransition()).get(tr.getTransition(), a);
			GeoLocation start = (tr.getType() == TerminalRoute.Type.SID) ? a : tx;
			if (tr.getType() == TerminalRoute.Type.STAR)
				tr.addWaypoint(tx);
			
			// Add the waypoints
			Collection<NavigationDataBean> wps = dao.getRouteWaypoints(ctx.getParameter("waypoints"), start);
			for (NavigationDataBean wp : wps)
				tr.addWaypoint(wp);
			
			// Add the transition if a SID
			if (tr.getType() == TerminalRoute.Type.SID)
				tr.addWaypoint(tx);
			
			// Start a transaction
			ctx.startTX();
			
			// Delete the old SID and replace with the new
			SetNavData wdao = new SetNavData(con);
			wdao.delete(otr);
			wdao.writeRoute(tr);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		return SC_OK;
	}

	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE always
	 */
	@Override
	public boolean isSecure() {
		return true;
	}
}