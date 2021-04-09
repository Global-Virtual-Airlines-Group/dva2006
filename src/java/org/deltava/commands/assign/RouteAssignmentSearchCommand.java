// Copyright 2012, 2017, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to build a Flight Assignment from a multi-leg route.
 * @author Luke
 * @version 10.0
 * @since 4.1
 */

public class RouteAssignmentSearchCommand extends AbstractCommand {
	
	private static final Collection<ComboAlias> INCLUDE_OPTS = ComboUtils.fromArray(new String[] {"All Flights", "Only Historic Flights", "Only Current Flights" }, 
			new String[] {Inclusion.ALL.name(), Inclusion.INCLUDE.name(), Inclusion.EXCLUDE.name() });
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get command result
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/assign/routeSearch.jsp");
		ctx.setAttribute("inclusionOpts", INCLUDE_OPTS, REQUEST);
		if (ctx.getParameter("airportD") == null) {
			result.setSuccess(true);	
			return;
		}
		
		// Get the route pair and type
		Inclusion allowHistoric = EnumUtils.parse(Inclusion.class, ctx.getParameter("includeHistoric"), Inclusion.ALL);
		int maxDistance = StringUtils.parse(ctx.getParameter("maxLength"), 0);
		ScheduleRoute rp = new ScheduleRoute(SystemData.getAirport(ctx.getParameter("airportD")), SystemData.getAirport(ctx.getParameter("airportA")));
		rp.setType((allowHistoric == Inclusion.ALL) ? RoutePairType.HYBRID : (allowHistoric == Inclusion.EXCLUDE) ? RoutePairType.PRESENT : RoutePairType.HISTORIC);
		try {
			Connection con = ctx.getConnection();
			
			// Load aircraft for the user
			GetAircraft acdao = new GetAircraft(con);
			Collection<Aircraft> myEQTypes = new TreeSet<Aircraft>(acdao.getAircraftTypes(ctx.getUser().getID()));
			
			// Load all route pairs
			RoutePathHelper rph = new RoutePathHelper(400, 800);
			GetRawSchedule rsdao = new GetRawSchedule(con);
			GetScheduleSearch sdao = new GetScheduleSearch(con);
			sdao.setSources(rsdao.getSources(true, ctx.getDB()));
			Collection<ScheduleRoute> lnks = sdao.getRoutePairs(allowHistoric);
			lnks.removeIf(sr -> ((maxDistance > 0) && (sr.getDistance() > maxDistance)));
			rph.setLinks(lnks);
			
			// Figure out the routes
			Collection<RoutePair> rts = rph.getShortestPath(rp);
			
			// Load the flights
			String airlineCode = SystemData.get("airline.code");
			Map<Airport, Collection<ScheduleEntry>> results = new LinkedHashMap<Airport, Collection<ScheduleEntry>>(); int totalDistance = 0;
			for (RoutePair rtp : rts) {
				totalDistance += rtp.getDistance();
				ScheduleSearchCriteria ssc = new ScheduleSearchCriteria("RAND()");
				ssc.setDBName(ctx.getDB());
				ssc.setAirportD(rtp.getAirportD());
				ssc.setAirportA(rtp.getAirportA());
				ssc.setLeg(0);
				ssc.setExcludeHistoric(allowHistoric);
				ssc.setCheckDispatchRoutes(true);
				myEQTypes.removeIf(ac -> filter(ac, rtp, airlineCode));
				Collection<ScheduleEntry> entries = sdao.search(ssc);
				Collection<ScheduleEntry> filteredEntries = entries.stream().filter(se -> filter(se, rp.getType())).collect(Collectors.toList());
				results.put(rtp.getAirportA(), filteredEntries.isEmpty() ? entries : filteredEntries);
			}
			
			// Save results
			ctx.setAttribute("results", results, REQUEST);
			ctx.setAttribute("myEQ", myEQTypes, REQUEST);
			ctx.setAttribute("totalDistance", Integer.valueOf(totalDistance), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status variables and forward
		ctx.setAttribute("rp", rp, REQUEST);
		ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
		result.setSuccess(true);
	}
	
	private static boolean filter(ScheduleEntry se, RoutePairType rt) {
		return (rt.hasHistoric() && se.getHistoric()) || (rt.hasCurrent() && !se.getHistoric());
	}
	
	private static boolean filter(Aircraft a, RoutePair rp, String airlineCode) {
		AircraftPolicyOptions opts = a.getOptions(airlineCode);
		return ((opts == null) || ((opts.getRange() > 0) && ((rp.getDistance() + 200) > opts.getRange())));
	}
}