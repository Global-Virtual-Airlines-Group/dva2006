// Copyright 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2020, 2021, 2022, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.assign.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.*;
import org.deltava.util.flightplan.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to create flight plans.
 * @author Luke
 * @version 11.5
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
		Airline a = SystemData.getAirline(ctx.getParameter("airline"));
		if (a == null)
			a = SystemData.getAirline(SystemData.get("airline.code"));
		
		Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
		Airport aA = SystemData.getAirport(ctx.getParameter("airportA"));
		String alt = ctx.getParameter("cruiseAlt");
		if (!StringUtils.isEmpty(alt) && (alt.startsWith("FL")))
			alt = alt.substring(2) + "00";
		if (StringUtils.parse(alt, -1) < 1000)
			alt = "35000";

		// Validate the airports
		Simulator sim = Simulator.fromName(ctx.getParameter("simVersion"), Simulator.P3Dv4);
		if (aD == null)
			throw error(SC_BAD_REQUEST, String.format("Invalid Departure Airport - %s", ctx.getParameter("airportD")), false);
		else if (aA == null)
			throw error(SC_BAD_REQUEST, String.format("Invalid Arrival Airport - %s", ctx.getParameter("airportA")), false);

		// Update the flight plan
		FlightPlanGenerator fpgen = FlightPlanGenerator.create(sim);
		fpgen.setAirline(a);
		fpgen.setAirports(aD, aA);
		fpgen.setCruiseAltitude(alt);

		// Check if saving in PIREP
		boolean saveDraft = Boolean.parseBoolean(ctx.getParameter("saveDraft"));
		StringBuilder rteBuf = new StringBuilder();

		Collection<NavigationDataBean> routePoints = new LinkedHashSet<NavigationDataBean>();
		routePoints.add(new AirportLocation(aD));
		try {
			Connection con = ctx.getConnection();
			GetNavRoute dao = new GetNavRoute(con);
			GetGates gdao = new GetGates(con);
			Gate gD = gdao.getGate(aD, ctx.getParameter("gateD"));
			Gate gA = gdao.getGate(aA, ctx.getParameter("gateA"));
			
			// Load the departure gate
			if (fpgen instanceof MSFSGenerator fsgen)
				fsgen.setGateD(gD);
			
			// Get the AIRAC cycle
			GetMetadata mddao = new GetMetadata(con);
			fpgen.setNavCycle(mddao.get("navdata.cycle"));

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
				boolean doPax = Boolean.parseBoolean(ctx.getParameter("precalcPax"));
				
				GetAircraft acdao = new GetAircraft(con);
				Aircraft ac = acdao.get(ctx.getParameter("eqType"));
				if (ac == null)
					ac = acdao.get(ctx.getUser().getEquipmentType());
				
				// Init the schedule DAO
				GetSchedule sdao = new GetSchedule(con);
				GetRawSchedule rsdao = new GetRawSchedule(con);
				sdao.setSources(rsdao.getSources(true, ctx.getDB()));
				ctx.startTX();
				
				// Load Draft flights
				GetFlightReports frdao = new GetFlightReports(con);
				ScheduleRoute rt = new ScheduleRoute(a, aD, aA);
				List<FlightReport> dFlights = frdao.getDraftReports(ctx.getUser().getID(), rt, ctx.getDB());
				
				AssignmentInfo ai = null;
				DraftFlightReport dfr = dFlights.isEmpty() ? null : (DraftFlightReport) dFlights.get(0);
				if (dfr == null) {
					ScheduleEntry schedInfo = sdao.getFlightNumber(rt, 12, ctx.getDB());
					if (schedInfo == null) {
						dfr = new DraftFlightReport(SystemData.getAirline(SystemData.get("airline.code")), ctx.getUser().getPilotNumber(), 1);
						dfr.setAirportD(aD);
						dfr.setAirportA(aA);
						dfr.setDate(Instant.now());
						dfr.setEquipmentType(ac.getName());
						dfr.setAuthorID(ctx.getUser().getID());
						dfr.addStatusUpdate(ctx.getUser().getID(), HistoryType.UPDATE, "Created via Route Plotter");
					} else {
						dfr = new DraftFlightReport(schedInfo);
						dfr.setDate(Instant.now());
						dfr.setEquipmentType(ac.getName());
						dfr.setTimeD(schedInfo.getTimeD().toLocalDateTime());
						dfr.setTimeA(schedInfo.getTimeA().toLocalDateTime());
						dfr.addStatusUpdate(ctx.getUser().getID(), HistoryType.UPDATE, String.format("Created via Route Plotter from %s", schedInfo.getShortCode()));
						
						// Create a flight assignment
						ai = new AssignmentInfo(ac.getName());
						ai.setPilotID(ctx.getUser().getID());
						ai.setStatus(AssignmentStatus.RESERVED);
						ai.setAssignDate(dfr.getDate());
						ai.addAssignment(new AssignmentLeg(dfr));
						ai.addFlight(dfr);
					}
				} else {
					PIREPAccessControl acc = new PIREPAccessControl(ctx, dfr);
					acc.validate();
					boolean canRecalcLoad = acc.getCanCalculateLoad();
					doPax &= canRecalcLoad;
					if (!canRecalcLoad)
						ctx.setHeader("X-Plan-No-Recalc", 1);
				}
					
				dfr.setAltitude(alt);
				dfr.setSimulator(sim);
				dfr.setRank(ctx.getUser().getRank());
				if (gD != null) dfr.setGateD(gD.getName());
				if (gA != null) dfr.setGateA(gA.getName());
				if (!newRoute.equals(dfr.getRoute()))
					dfr.addStatusUpdate(ctx.getUser().getID(), HistoryType.UPDATE, "Updated Route via Route Plotter");
				
				// Calculate load factor if requested
				if (doPax) {
					boolean hasPax = (dfr.getPassengers() > 0);
					EconomyInfo eInfo = (EconomyInfo) SystemData.getObject(SystemData.ECON_DATA);
					AircraftPolicyOptions opts = ac.getOptions(SystemData.get("airline.code"));
					if ((eInfo != null) && (opts != null)) {
						LoadFactor lf = new LoadFactor(eInfo);
						double loadFactor = lf.generate(dfr.getDate());
						dfr.setPassengers((int) Math.round(opts.getSeats() * loadFactor));
						dfr.setLoadFactor(loadFactor);
						dfr.setDate(Instant.now()); // update load calculation date
						dfr.addStatusUpdate(ctx.getUser().getID(), HistoryType.UPDATE, (hasPax ? "Updated" : "Requested") + " pre-flight Load Factor");
						ctx.setHeader("X-Plan-No-Recalc", 1); // cannot recalculate again today
						ctx.setHeader("X-Plan-Pax", dfr.getPassengers());
					}
				}
				
				// Save the flight assignment
				if (ai != null) {
					SetAssignment awdao = new SetAssignment(con);
					awdao.write(ai, ctx.getDB());
				}
				
				// Save the route and commit
				SetFlightReport frwdao = new SetFlightReport(con);
				dfr.setRoute(newRoute);
				frwdao.write(dfr);
				ctx.commitTX();
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// No download override
		if (Boolean.parseBoolean(ctx.getParameter("noDL"))) {
			ctx.setHeader("X-Plan-Empty", 1);
			return SC_OK;
		}

		// Flush the output buffer
		String fileName = String.format("%s-%s.%s", aD.getICAO(), aA.getICAO(), fpgen.getExtension());
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

	@Override
	public boolean isSecure() {
		return true;
	}
}