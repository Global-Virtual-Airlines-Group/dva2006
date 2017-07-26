// Copyright 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to build a Flight Assignment from a multi-leg route.
 * @author Luke
 * @version 7.5
 * @since 4.1
 */

public class RouteAssignmentSearchCommand extends AbstractCommand {
	
	private static final int DEFAULT_COST = 400;
	private static final Collection<ComboAlias> BIAS_OPTS = ComboUtils.fromArray(new String[] { "Avoid Historic Routes",  "No Preference", "Prefer Historic Routes"}, new String[] {"850", String.valueOf(DEFAULT_COST), "50"});
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get command result
		CommandResult result = ctx.getResult();
		ctx.setAttribute("biasOpts", BIAS_OPTS, REQUEST);
		result.setURL("/jsp/assign/routeSearch.jsp");
		if (ctx.getParameter("airportD") == null) {
			result.setSuccess(true);	
			return;
		}
		
		// Get the route pair and type
		int historicCost = StringUtils.parse(ctx.getParameter("historicBias"), DEFAULT_COST);
		ScheduleRoute rp = new ScheduleRoute(SystemData.getAirport(ctx.getParameter("airportD")), SystemData.getAirport(ctx.getParameter("airportA")));
		rp.setType((historicCost == DEFAULT_COST) ? RoutePairType.HYBRID : (historicCost > DEFAULT_COST) ? RoutePairType.PRESENT : RoutePairType.HISTORIC);
		try {
			Connection con = ctx.getConnection();
			
			// Load aircraft for the user
			Collection<Aircraft> myEQTypes = new TreeSet<Aircraft>();
			GetAircraft acdao = new GetAircraft(con);
			for (String eqType : ctx.getUser().getRatings()) {
				Aircraft ac = acdao.get(eqType);
				myEQTypes.add(ac);
			}
			
			// Load all route pairs
			RoutePathHelper rph = new RoutePathHelper(400, historicCost);
			GetScheduleSearch sdao = new GetScheduleSearch(con);
			rph.setLinks(sdao.getRoutePairs());
			
			// Figure out the routes
			Collection<RoutePair> rts = rph.getShortestPath(rp);
			
			// Load the flights
			Map<Airport, Collection<ScheduleEntry>> results = new LinkedHashMap<Airport, Collection<ScheduleEntry>>(); int totalDistance = 0;
			for (RoutePair rtp : rts) {
				totalDistance += rtp.getDistance();
				ScheduleSearchCriteria ssc = new ScheduleSearchCriteria("RAND()");
				ssc.setDBName(SystemData.get("airline.db"));
				ssc.setAirportD(rtp.getAirportD());
				ssc.setAirportA(rtp.getAirportA());
				ssc.setLeg(0);
				ssc.setCheckDispatchRoutes(true);
				for (Iterator<Aircraft> i = myEQTypes.iterator(); i.hasNext(); ) {
					Aircraft a = i.next();
					if ((a.getRange() > 0) && ((rtp.getDistance() + 250) > a.getRange()))
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