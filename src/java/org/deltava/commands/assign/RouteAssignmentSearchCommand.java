// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.GeoUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to build a Flight Assignment from a multi-leg route.
 * @author Luke
 * @version 4.1
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
			Collection<Aircraft> myEQTypes = new ArrayList<Aircraft>();
			GetAircraft acdao = new GetAircraft(con);
			for (String eqType : ctx.getUser().getRatings())
				myEQTypes.add(acdao.get(eqType));

			// Find the route
			GetScheduleRouteSearch rsdao = new GetScheduleRouteSearch(con);
			List<Airport> airports = rsdao.findRoute(rp);
			if (!airports.isEmpty()) {
				Map<Airport, Collection<ScheduleEntry>> results = new LinkedHashMap<Airport, Collection<ScheduleEntry>>();
				Airport lastA = airports.get(0);
				GetScheduleSearch sdao = new GetScheduleSearch(con);
				for (int x = 1; x < airports.size(); x++) {
					Airport nextA = airports.get(x);
					ScheduleSearchCriteria ssc = new ScheduleSearchCriteria("RAND()");
					ssc.setDBName(SystemData.get("airline.db"));
					ssc.setAirportD(lastA);
					ssc.setAirportA(nextA);
					ssc.setLeg(0);
					ssc.setCheckDispatchRoutes(true);
					results.put(nextA, sdao.search(ssc));
					
					// Prune equipment types that don't match the range
					int distance = GeoUtils.distance(lastA, nextA);
					for (Iterator<Aircraft> i = myEQTypes.iterator(); i.hasNext(); ) {
						Aircraft a = i.next();
						if ((a.getRange() > 0) && ((distance + 200) > a.getRange()))
							i.remove();
					}
					
					lastA = nextA;
				}

				// Save results
				ctx.setAttribute("results", results, REQUEST);
				ctx.setAttribute("myEQ", myEQTypes, REQUEST);
			}
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