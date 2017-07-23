// Copyright 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to build a Flight Assignment from a multi-leg route.
 * @author Luke
 * @version 7.5
 * @since 4.1
 */

public class RouteAssignmentSearchCommand extends AbstractCommand {

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
		if (ctx.getParameter("airportD") == null) {
			result.setSuccess(true);	
			return;
		}
		
		// Get the route pair
		RoutePair rp = new ScheduleRoute(SystemData.getAirport(ctx.getParameter("airportD")), SystemData.getAirport(ctx.getParameter("airportA")));
		try {
			Connection con = ctx.getConnection();
			
			// Load aircraft for the user
			int maxRange = 0; Collection<Aircraft> myEQTypes = new TreeSet<Aircraft>();
			GetAircraft acdao = new GetAircraft(con);
			for (String eqType : ctx.getUser().getRatings()) {
				Aircraft ac = acdao.get(eqType);
				maxRange = Math.max(maxRange, ac.getRange());
				myEQTypes.add(ac);
			}
			
			// Load all route pairs
			RoutePathHelper rph = new RoutePathHelper();
			GetScheduleSearch sdao = new GetScheduleSearch(con);
			Map<Airport, Collection<Airport>> routes = sdao.getRoutePairs();
			routes.forEach((k, v) -> rph.setLinks(k, v));
			
			// Figure out the routes
			Collection<RoutePair> rts = rph.getShortestPath(rp);
			
			// Load the flights
			Map<Airport, Collection<ScheduleEntry>> results = new LinkedHashMap<Airport, Collection<ScheduleEntry>>();
			for (RoutePair rtp : rts) {
				ScheduleSearchCriteria ssc = new ScheduleSearchCriteria("RAND()");
				ssc.setDBName(SystemData.get("airline.db"));
				ssc.setAirportD(rp.getAirportD());
				ssc.setAirportA(rp.getAirportA());
				ssc.setLeg(0);
				ssc.setCheckDispatchRoutes(true);
				for (Iterator<Aircraft> i = myEQTypes.iterator(); i.hasNext(); ) {
					Aircraft a = i.next();
					if ((a.getRange() > 0) && ((rtp.getDistance() + 200) > a.getRange()))
						i.remove();
				}
				
				results.put(rtp.getAirportA(), sdao.search(ssc));
			}
			
			// Save results
			ctx.setAttribute("results", results, REQUEST);
			ctx.setAttribute("myEQ", myEQTypes, REQUEST);
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
}