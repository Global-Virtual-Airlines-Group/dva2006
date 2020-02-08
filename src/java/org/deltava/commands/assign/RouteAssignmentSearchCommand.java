// Copyright 2012, 2017, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 9.0
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
		ScheduleRoute rp = new ScheduleRoute(SystemData.getAirport(ctx.getParameter("airportD")), SystemData.getAirport(ctx.getParameter("airportA")));
		rp.setType((allowHistoric == Inclusion.ALL) ? RoutePairType.HYBRID : (allowHistoric == Inclusion.EXCLUDE) ? RoutePairType.PRESENT : RoutePairType.HISTORIC);
		try {
			Connection con = ctx.getConnection();
			
			// Load aircraft for the user
			GetAircraft acdao = new GetAircraft(con);
			Collection<Aircraft> myEQTypes = new TreeSet<Aircraft>(acdao.getAircraftTypes(ctx.getUser().getID()));
			
			// Load all route pairs
			RoutePathHelper rph = new RoutePathHelper(400, 800);
			GetScheduleSearch sdao = new GetScheduleSearch(con);
			rph.setLinks(sdao.getRoutePairs(allowHistoric));
			
			// Figure out the routes
			Collection<RoutePair> rts = rph.getShortestPath(rp);
			
			// Load the flights
			String airlineCode = SystemData.get("airline.code");
			Map<Airport, Collection<ScheduleEntry>> results = new LinkedHashMap<Airport, Collection<ScheduleEntry>>(); int totalDistance = 0;
			for (RoutePair rtp : rts) {
				totalDistance += rtp.getDistance();
				ScheduleSearchCriteria ssc = new ScheduleSearchCriteria("RAND()");
				ssc.setDBName(SystemData.get("airline.db"));
				ssc.setAirportD(rtp.getAirportD());
				ssc.setAirportA(rtp.getAirportA());
				ssc.setLeg(0);
				ssc.setExcludeHistoric(allowHistoric);
				ssc.setCheckDispatchRoutes(true);
				for (Iterator<Aircraft> i = myEQTypes.iterator(); i.hasNext(); ) {
					Aircraft a = i.next();
					AircraftPolicyOptions opts = a.getOptions(airlineCode);
					if ((opts.getRange() > 0) && ((rtp.getDistance() + 250) > opts.getRange()))
						i.remove();
				}
				
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
}