// Copyright 2005, 2007, 2008, 2009, 2010, 2012, 2016, 2017, 2018, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.Simulator;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.wx.METAR;

import org.deltava.commands.*;
import org.deltava.comparators.RunwayComparator;
import org.deltava.dao.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to plot a flight route.
 * @author Luke
 * @version 9.0
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
			
			// Look for a draft PIREP
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport dfr = frdao.get(ctx.getID());
			if ((dfr != null) && (dfr.getDatabaseID(DatabaseID.PILOT) == ctx.getUser().getID())) {
				ctx.setAttribute("flight", dfr, REQUEST);
				ctx.setAttribute("airlines", Collections.singleton(dfr.getAirline()), REQUEST);
				ctx.setAttribute("airportsD", getAirports(dfr.getAirportD()), REQUEST);
				ctx.setAttribute("airportsA", getAirports(dfr.getAirportA()), REQUEST);
				if (dfr.getSimulator() != Simulator.UNKNOWN)
					ctx.setAttribute("sim", dfr.getSimulator(), REQUEST);
				
				// Get aircraft profile and SID runways
				GetNavRoute navdao = new GetNavRoute(con);
				Aircraft a = acdao.get(dfr.getEquipmentType());
				AircraftPolicyOptions opts = a.getOptions(SystemData.get("airline.code"));
				Collection<String> sidRwys = navdao.getSIDRunways(dfr.getAirportD());
				
				// Get departure runways
				GetACARSRunways rwdao = new GetACARSRunways(con);
				List<Runway> runways = rwdao.getPopularRunways(dfr.getAirportD(), dfr.getAirportA(), true).stream().filter(r -> r.getLength() > opts.getTakeoffRunwayLength()).filter(r -> sidRwys.contains("RW" + r.getName())).collect(Collectors.toList());
				
				// Sort based on wind
				GetWeather wxdao = new GetWeather(con);
				METAR wxD = wxdao.getMETAR(dfr.getAirportD());
				if ((wxD != null) && (wxD.getWindSpeed() > 0))
					runways = CollectionUtils.sort(runways, new RunwayComparator(wxD.getWindDirection(), wxD.getWindSpeed()));
				
				// Save runways and best runway
				Runway rwyD = runways.isEmpty() ? null : runways.get(0);
				ctx.setAttribute("dRwys", runways, REQUEST);
				ctx.setAttribute("rwy", rwyD, REQUEST);
				
				// Load SID/STARs
				ctx.setAttribute("sids", navdao.getRoutes(dfr.getAirportD(), TerminalRoute.Type.SID), REQUEST);
				ctx.setAttribute("stars", navdao.getRoutes(dfr.getAirportA(), TerminalRoute.Type.STAR), REQUEST);
				
				// Load up route
				if (!StringUtils.isEmpty(dfr.getRoute())) {
					List<String> wps = StringUtils.split(dfr.getRoute(), " ");
					if (TerminalRoute.isNameValid(wps.get(0))) {
						if ((rwyD != null) && (wps.size() > 1))
							ctx.setAttribute("sid", navdao.getBestRoute(dfr.getAirportD(), TerminalRoute.Type.SID, wps.get(0), wps.get(1), rwyD), REQUEST);
						
						wps.remove(0);
					} if ((wps.size() > 1) && TerminalRoute.isNameValid(wps.get(wps.size() - 1))) {
						ctx.setAttribute("star", navdao.getBestRoute(dfr.getAirportA(), TerminalRoute.Type.STAR, wps.get(wps.size() -1), wps.get(wps.size() - 2), (String)null), REQUEST);
						wps.remove(wps.size() - 1);
					}
					
					dfr.setRoute(StringUtils.listConcat(wps, " "));
				}
			} else {
				ctx.setAttribute("airlines", SystemData.getAirlines().values(), REQUEST);
				ctx.setAttribute("airportsD", Collections.emptyList(), REQUEST);
				ctx.setAttribute("airportsA", Collections.emptyList(), REQUEST);
				
				// Load the previous approved/submitted flight report
				frdao.setQueryMax(15);
				List<FlightReport> results = frdao.getByPilot(ctx.getUser().getID(), new ScheduleSearchCriteria("SUBMITTED DESC"));
				Optional<FlightReport> fr = results.stream().filter(fl -> (fl.getStatus() != FlightStatus.DRAFT) && (fl.getStatus() != FlightStatus.REJECTED)).findFirst();
				if (fr.isPresent())
					ctx.setAttribute("sim", fr.get().getSimulator(), REQUEST);
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