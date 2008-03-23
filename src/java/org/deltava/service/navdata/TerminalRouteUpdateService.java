// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 2.1
 * @since 2.1
 */

public class TerminalRouteUpdateService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Validate access
		if (!ctx.isUserInRole("Schedule"))
			throw error(SC_UNAUTHORIZED, "Not in Schedule role");
		
		// Get the airport
		Airport a = SystemData.getAirport(ctx.getParameter("icao"));
		if (a == null)
			throw error(SC_NOT_FOUND, "Unknown Airport - " + ctx.getParameter("icao"));
		
		// Build the Terminal Route
		int type = "sid".equalsIgnoreCase(ctx.getParameter("type")) ? TerminalRoute.SID : TerminalRoute.STAR;
		TerminalRoute tr = new TerminalRoute(a.getICAO(), ctx.getParameter("name"), type);
		tr.setTransition(ctx.getParameter("transition"));
		tr.setRunway(ctx.getParameter("runway"));
		tr.setCanPurge(Boolean.valueOf(ctx.getParameter("canPurge")).booleanValue());
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the existing SID
			GetNavRoute dao = new GetNavRoute(con);
			TerminalRoute otr = dao.getRoute(a, type, tr.getName());
			
			// Get the transition
			NavigationDataBean tx = dao.get(tr.getTransition()).get(tr.getTransition(), a);
			GeoLocation start = (tr.getType() == TerminalRoute.SID) ? a : tx;
			if (tr.getType() == TerminalRoute.STAR)
				tr.addWaypoint(tx);
			
			// Add the waypoints
			Collection<NavigationDataBean> wps = dao.getRouteWaypoints(ctx.getParameter("waypoints"), start);
			for (NavigationDataBean wp : wps)
				tr.addWaypoint(wp);
			
			// Add the transition if a SID
			if (tr.getType() == TerminalRoute.SID)
				tr.addWaypoint(tx);
			
			// Start a transaction
			ctx.startTX();
			
			// Delete the old SID and replace with the new
			SetNavData wdao = new SetNavData(con);
			wdao.delete(otr);
			wdao.writeRoute(tr);
			
			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Return success code
		return SC_OK;
	}

	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE always
	 */
	public boolean isSecure() {
		return true;
	}
}