// Copyright 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.flightplan.FlightPlanGenerator;

/**
 * A Web Service to generate a Flight Plan from a draft Flight Report.
 * @author Luke
 * @version 10.0
 * @since 8.0
 */

public class DraftFlightPlanService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		FlightPlanGenerator gen = null; RouteBuilder rb = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the flight report
			int id = StringUtils.parse(ctx.getParameter("id"), 0);
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport fr = frdao.get(id, ctx.getDB());
			if (fr == null)
				throw error(SC_BAD_REQUEST, "Invalid Flight Report ID - " + id, false);

			// Create a route builder
			rb = new RouteBuilder(fr, fr.getRoute());
			
			// Get the flight plan generator
			gen = FlightPlanGenerator.create(fr.getSimulator());
			gen.setAirline(fr.getAirline());
			gen.setAirports(fr.getAirportD(), fr.getAirportA());
			
			// Calculate the approx cruise altitude
			double rawHdg = GeoUtils.course(fr.getAirportD(), fr.getAirportA());
			gen.setCruiseAltitude((rawHdg >= 180) ? "FL340" : "FL350");
			
			// Load the SID
			GetNavRoute dao = new GetNavRoute(con);
			TerminalRoute sid = dao.getRoute(fr.getAirportD(), TerminalRoute.Type.SID, rb.getSID());
			if (sid != null) {
				rb.add(sid);
				gen.setSID(sid);
			}
			
			// Add the route waypoints
			if (!StringUtils.isEmpty(fr.getRoute())) {
				gen.setRoute(fr.getRoute());
				List<NavigationDataBean> points = dao.getRouteWaypoints(fr.getRoute(), fr.getAirportD());
				GeoUtils.stripDetours(points, 60).forEach(rb::add);
			}
			
			// Load the STAR
			TerminalRoute star = dao.getRoute(fr.getAirportA(), TerminalRoute.Type.STAR, rb.getSTAR());
			if (star != null) {
				gen.setSTAR(star);
				rb.add(star);
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Flush the output buffer
		String fileName = rb.getAirportD().getICAO() + "-" + rb.getAirportA().getICAO() + "." + gen.getExtension();
		try {
			ctx.setContentType(gen.getMimeType(), gen.getEncoding());
			ctx.setHeader("X-Plan-Filename", fileName);
			ctx.setHeader("Content-disposition", "attachment; filename=" + fileName);
			ctx.println(gen.generate(rb.getPoints()));
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error", false);
		}
		
		return SC_OK;
	}

	@Override
	public boolean isSecure() {
		return true;
	}
}