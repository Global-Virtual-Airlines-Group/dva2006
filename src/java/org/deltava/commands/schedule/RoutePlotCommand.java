// Copyright 2005, 2007, 2008, 2009, 2010, 2012, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.Simulator;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.RunwayUsage;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to plot a flight route.
 * @author Luke
 * @version 11.6
 * @since 1.0
 */

public class RoutePlotCommand extends AbstractCommand {
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Load aircraft types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("eqTypes", acdao.getAircraftTypes(SystemData.get("airline.code")), REQUEST);
			
			// Determine our default simulator
			GetFlightReports frdao = new GetFlightReports(con);
			frdao.setQueryMax(15);
			List<FlightReport> results = frdao.getByPilot(ctx.getUser().getID(), new LogbookSearchCriteria("SUBMITTED DESC", ctx.getDB()));
			Optional<FlightReport> fr = results.stream().filter(fl -> (fl.getStatus() != FlightStatus.DRAFT) && (fl.getStatus() != FlightStatus.REJECTED)).findFirst();
			Simulator sim = fr.isPresent() ? fr.get().getSimulator() : Simulator.UNKNOWN;
			ctx.setAttribute("sim", sim, REQUEST);
			
			// Look for a draft PIREP
			DraftFlightReport dfr = frdao.getDraft(ctx.getID(), ctx.getDB());
			if (dfr != null) {
				ctx.setAttribute("flight", dfr, REQUEST);
				ctx.setAttribute("airlines", Collections.singleton(dfr.getAirline()), REQUEST);
				ctx.setAttribute("airportsD", getAirports(dfr.getAirportD()), REQUEST);
				ctx.setAttribute("airportsA", getAirports(dfr.getAirportA()), REQUEST);
				ctx.setAttribute("alSize", Integer.valueOf(dfr.getAirline().getCode().length()), REQUEST);
				sim = dfr.getSimulator();
				
				// Check if load can be calculated
				PIREPAccessControl ac = new PIREPAccessControl(ctx, dfr);
				ac.validate();
				ctx.setAttribute("allowLoad", Boolean.valueOf(ac.getCanCalculateLoad()), REQUEST);
				
				// Load gates
				GetGates gdao = new GetGates(con);
				Gate gD = gdao.getGate(dfr.getAirportD(), dfr.getGateD());
				Gate gA = gdao.getGate(dfr.getAirportA(), dfr.getGateA());
				ctx.setAttribute("gatesD", CollectionUtils.nonNull(gD), REQUEST);
				ctx.setAttribute("gatesA", CollectionUtils.nonNull(gA), REQUEST);
				
				// Get aircraft profile
				Aircraft a = acdao.get(dfr.getEquipmentType());
				AircraftPolicyOptions opts = a.getOptions(SystemData.get("airline.code"));
				
				// Initialize the runway helper
				RunwayHelper rh = new RunwayHelper(dfr, opts);
				GetNavRoute navdao = new GetNavRoute(con);
				rh.addSIDs(navdao.getRoutes(dfr.getAirportD(), TerminalRoute.Type.SID));
				rh.addSTARs(navdao.getRoutes(dfr.getAirportA(), TerminalRoute.Type.STAR));
				
				// Get popular runways
				GetRunwayUsage rwdao = new GetRunwayUsage(con);
				RunwayUsage dru = rwdao.getPopularRunways(dfr, true);
				RunwayUsage aru = rwdao.getPopularRunways(dfr, false);
				List<RunwayUse> dr = dru.apply(navdao.getRunways(dfr.getAirportD(), Simulator.P3Dv4));
				List<RunwayUse> ar = aru.apply(navdao.getRunways(dfr.getAirportA(), Simulator.P3Dv4));
				rh.addRunways(dr, ar);

				// Get weather data
				GetWeather wxdao = new GetWeather(con);
				rh.setMETAR(wxdao.getMETAR(dfr.getAirportD()), wxdao.getMETAR(dfr.getAirportA()));
				
				// Get runways
				List<RunwayUse> dRwys = rh.getRunways(true); 
				List<RunwayUse> aRwys = rh.getRunways(false);
				
				// Save runways and best runway
				Runway rwyD = dRwys.isEmpty() ? null : dRwys.getFirst();
				ctx.setAttribute("dRwys", dRwys, REQUEST);
				ctx.setAttribute("aRwyNames", aRwys.stream().map(Runway::getName).collect(Collectors.toList()), REQUEST);
				ctx.setAttribute("rwy", rwyD, REQUEST);
				
				// Save SID/STARs
				ctx.setAttribute("sids", rh.getSIDs(), REQUEST);
				ctx.setAttribute("stars", rh.getSTARs(), REQUEST);
				
				// Calculate altitude
				if (StringUtils.isEmpty(dfr.getAltitude())) {
					double hdg = GeoUtils.course(dfr.getAirportD(), dfr.getAirportA());
					if (hdg < 180)
						dfr.setAltitude(String.valueOf((dfr.getDistance() < 250) ? 25000 : 35000));
					else
						dfr.setAltitude(String.valueOf((dfr.getDistance() < 250) ? 24000 : 34000));
				}
				
				// Load up route
				if (!StringUtils.isEmpty(dfr.getRoute())) {
					List<String> wps = StringUtils.split(dfr.getRoute(), " ");
					if (TerminalRoute.isNameValid(wps.getFirst())) {
						if ((rwyD != null) && (wps.size() > 1))
							ctx.setAttribute("sid", navdao.getBestRoute(dfr.getAirportD(), TerminalRoute.Type.SID, wps.getFirst(), wps.get(1), rwyD), REQUEST);
						
						wps.remove(0);
					} if ((wps.size() > 1) && TerminalRoute.isNameValid(wps.getLast())) {
						ctx.setAttribute("star", navdao.getBestRoute(dfr.getAirportA(), TerminalRoute.Type.STAR, wps.getLast(), wps.get(wps.size() - 2), (String)null), REQUEST);
						wps.remove(wps.size() - 1);
					}
					
					dfr.setRoute(StringUtils.listConcat(wps, " "));
				}
			} else {
				ctx.setAttribute("airportsD", Collections.emptyList(), REQUEST);
				ctx.setAttribute("airportsA", Collections.emptyList(), REQUEST);
				ctx.setAttribute("gatesD", Collections.emptyList(), REQUEST);
				ctx.setAttribute("gatesA", Collections.emptyList(), REQUEST);
				ctx.setAttribute("airlines", SystemData.getAirlines(), REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/routePlot.jsp");
		result.setSuccess(true);
	}
	
	/*
	 * Helper method to add superceded airports.
	 */
	private static Collection<Airport> getAirports(Airport a) {
		Collection<Airport> results = new LinkedHashSet<Airport>();
		results.add(a);
		if (!StringUtils.isEmpty(a.getSupercededAirport()))
			results.add(SystemData.getAirport(a.getSupercededAirport()));
		
		return results;
	}
}