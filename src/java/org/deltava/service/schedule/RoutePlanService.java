// Copyright 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.assign.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.flightplan.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to create flight plans.
 * @author Luke
 * @version 9.1
 * @since 2.2
 */

public class RoutePlanService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the airports and altitude
		Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
		Airport aA = SystemData.getAirport(ctx.getParameter("airportA"));
		String alt = ctx.getParameter("cruiseAlt");
		if (!StringUtils.isEmpty(alt) && (alt.startsWith("FL")))
			alt = alt.substring(2) + "00";
		if (StringUtils.parse(alt, -1) < 1000)
			alt = "35000";

		// Validate the airports
		Simulator sim = Simulator.fromName(ctx.getParameter("simVersion"), Simulator.FSX);
		if (aD == null)
			throw error(SC_BAD_REQUEST, "Invalid Departure Airport - " + ctx.getParameter("airportD"), false);
		else if (aA == null)
			throw error(SC_BAD_REQUEST, "Invalid Arrival Airport - " + ctx.getParameter("airportA"), false);

		// Update the flight plan
		FlightPlanGenerator fpgen = FlightPlanGenerator.create(sim);
		fpgen.setAirline(SystemData.getAirline(ctx.getParameter("airline")));
		fpgen.setAirports(aD, aA);
		fpgen.setCruiseAltitude(alt);

		// Check if saving in PIREP
		boolean saveDraft = Boolean.valueOf(ctx.getParameter("saveDraft")).booleanValue();
		StringBuilder rteBuf = new StringBuilder();

		Collection<NavigationDataBean> routePoints = new LinkedHashSet<NavigationDataBean>();
		routePoints.add(new AirportLocation(aD));
		try {
			Connection con = ctx.getConnection();
			GetNavRoute dao = new GetNavRoute(con);
			GetGates gdao = new GetGates(con);
			
			// Load the departure gate
			if (fpgen instanceof MSFSGenerator) {
				MSFSGenerator fsgen = (MSFSGenerator) fpgen;
				Gate gD = gdao.getGate(aD, sim, ctx.getParameter("gateD"));
				fsgen.setGateD(gD);
			}

			// Load the SID
			TerminalRoute sid = dao.getRoute(aD, TerminalRoute.Type.SID, ctx.getParameter("sid"));
			if (sid != null) {
				routePoints.addAll(sid.getWaypoints());
				fpgen.setSID(sid);
				rteBuf.append(sid.getName());
				rteBuf.append(' ');
			}

			// Add the route waypoints
			String rte = ctx.getParameter("route");
			if (!StringUtils.isEmpty(rte)) {
				fpgen.setRoute(rte);
				List<NavigationDataBean> points = dao.getRouteWaypoints(rte, aD);
				routePoints.addAll(GeoUtils.stripDetours(points, 60));
				rteBuf.append(rte);
			}

			// Load the STAR
			TerminalRoute star = dao.getRoute(aA, TerminalRoute.Type.STAR, ctx.getParameter("star"));
			if (star != null) {
				fpgen.setSTAR(star);
				routePoints.addAll(star.getWaypoints());
				if (rteBuf.indexOf(star.getTransition()) == -1) {
					rteBuf.append(' ');
					rteBuf.append(star.getTransition());
				}
					
				rteBuf.append(' ');
				rteBuf.append(star.getName());
			}

			// Add the destination airport
			routePoints.add(new AirportLocation(aA));
			
			// If we're saving a draft PIREP
			String newRoute = rteBuf.toString(); 
			if (saveDraft) {
				GetAircraft acdao = new GetAircraft(con);
				Aircraft ac = acdao.get(ctx.getParameter("eqType"));
				if (ac == null)
					ac = acdao.get(ctx.getUser().getEquipmentType());
				
				GetSchedule sdao = new GetSchedule(con);
				GetFlightReports frdao = new GetFlightReports(con);
				ScheduleRoute rt = new ScheduleRoute(aD, aA);
				List<FlightReport> dFlights = frdao.getDraftReports(ctx.getUser().getID(), rt, SystemData.get("airline.db"));
				ctx.startTX();
				
				AssignmentInfo ai = null;
				FlightReport dfr = dFlights.isEmpty() ? null : dFlights.get(0);
				if (dfr == null) {
					ScheduleEntry schedInfo = sdao.getFlightNumber(rt, SystemData.get("airline.db"));
					if (schedInfo == null) {
						dfr = new DraftFlightReport(SystemData.getAirline(SystemData.get("airline.code")), ctx.getUser().getPilotNumber(), 1);
						dfr.setAirportD(aD);
						dfr.setAirportA(aD);
						dfr.setEquipmentType(ac.getName());
						dfr.setAuthorID(ctx.getUser().getID());
					} else {
						dfr = new DraftFlightReport(schedInfo);
						dfr.setEquipmentType(ac.getName());
						
						// Create a flight assignment
						ai = new AssignmentInfo(ac.getName());
						ai.setPilotID(ctx.getUser());
						ai.setStatus(AssignmentStatus.RESERVED);
						ai.setAssignDate(Instant.now());
						ai.addAssignment(new AssignmentLeg(dfr));
						ai.addFlight(dfr);
					}
				}
					
				dfr.setSimulator(sim);
				dfr.setRank(ctx.getUser().getRank());
				dfr.setDate(Instant.now());
				if (dfr.getID() == 0)
					dfr.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, "Created via Route Plotter");
				else if (!newRoute.equals(dfr.getRoute()))
					dfr.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, "Updated Route via Route Plotter");
				
				// Save the flight assignment
				if (ai != null) {
					SetAssignment awdao = new SetAssignment(con);
					awdao.write(ai, SystemData.get("airline.db"));
				}
				
				// Save the route and commit
				SetFlightReport frwdao = new SetFlightReport(con);
				dfr.setRoute(newRoute);
				frwdao.write(dfr);
				ctx.commitTX();
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Flush the output buffer
		String fileName = aD.getICAO() + "-" + aA.getICAO() + "." + fpgen.getExtension();
		try {
			ctx.setContentType(fpgen.getMimeType(), fpgen.getEncoding());
			ctx.setHeader("X-Plan-Filename", fileName);
			ctx.setHeader("Content-disposition", "attachment; filename=" + fileName);
			ctx.println(fpgen.generate(routePoints));
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error", false);
		}

		return SC_OK;
	}

	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE
	 */
	@Override
	public boolean isSecure() {
		return true;
	}
}