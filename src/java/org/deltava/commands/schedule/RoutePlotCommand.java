// Copyright 2005, 2007, 2008, 2009, 2010, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 7.2
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
			
			// Look for a draft PIREP
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport dfr = frdao.get(ctx.getID());
			if ((dfr != null) && (dfr.getDatabaseID(DatabaseID.PILOT) == ctx.getUser().getID())) {
				ctx.setAttribute("flight", dfr, REQUEST);
				ctx.setAttribute("airlines", Collections.singleton(dfr.getAirline()), REQUEST);
				ctx.setAttribute("airportsD", Collections.singleton(dfr.getAirportD()), REQUEST);
				ctx.setAttribute("airportsA", Collections.singleton(dfr.getAirportA()), REQUEST);
				if (dfr.getSimulator() != Simulator.UNKNOWN)
					ctx.setAttribute("sim", dfr.getSimulator(), REQUEST);
				
				// Get aircraft profile and SID runways
				GetAircraft acdao = new GetAircraft(con);
				GetNavRoute navdao = new GetNavRoute(con);
				Aircraft a = acdao.get(dfr.getEquipmentType());
				Collection<String> sidRwys = navdao.getSIDRunways(dfr.getAirportD());
				
				// Get departure runways
				GetACARSRunways rwdao = new GetACARSRunways(con);
				List<Runway> runways = rwdao.getPopularRunways(dfr.getAirportD(), dfr.getAirportA(), true).stream().filter(r -> r.getLength() > a.getTakeoffRunwayLength())
						.filter(r -> sidRwys.contains("RW" + r.getName())).collect(Collectors.toList());
				
				// Sort based on wind
				GetWeather wxdao = new GetWeather(con);
				METAR wxD = wxdao.getMETAR(dfr.getAirportD());
				if ((wxD != null) && (wxD.getWindSpeed() > 0))
					runways = CollectionUtils.sort(runways, new RunwayComparator(wxD.getWindDirection(), wxD.getWindSpeed()));

				// Save runways and best runway
				ctx.setAttribute("dRwys", runways, REQUEST);
				if (runways.size() > 0)
					ctx.setAttribute("rwy", runways.get(0), REQUEST);
				
				// Load up route
				if (!StringUtils.isEmpty(dfr.getRoute())) {
					List<String> wps = StringUtils.split(dfr.getRoute(), " ");
					if (TerminalRoute.isNameValid(wps.get(0)))
						wps.remove(0);
					if ((wps.size() > 1) && TerminalRoute.isNameValid(wps.get(wps.size() - 1)))
						wps.remove(wps.size() - 1);
					
					dfr.setRoute(StringUtils.listConcat(wps, " "));
				}
			} else {
				ctx.setAttribute("airlines", SystemData.getAirlines().values(), REQUEST);
				ctx.setAttribute("airportsD", Collections.emptyList(), REQUEST);
				ctx.setAttribute("airportsA", Collections.emptyList(), REQUEST);
				
				// Load the previous approved/submitted flight report
				frdao.setQueryMax(10);
				List<FlightReport> results = frdao.getByPilot(ctx.getUser().getID(), new ScheduleSearchCriteria("SUBMITTED DESC"));
				for (FlightReport fr : results) {
					if ((fr.getStatus() != FlightReport.DRAFT) && (fr.getStatus() != FlightReport.REJECTED)) {
						ctx.setAttribute("sim", fr.getSimulator(), REQUEST);
						break;
					}
				}
			}
			
			// Load aircraft types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("eqTypes", acdao.getAircraftTypes(SystemData.get("airline.code")), REQUEST);
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
}