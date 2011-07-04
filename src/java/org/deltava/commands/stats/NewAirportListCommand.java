// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display airports a Pilot has not flown to.
 * @author Luke
 * @version 4.0
 * @since 4.0
 */

public class NewAirportListCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the default airline
		String defaultCode = SystemData.get("airline.code");
		Collection<Airport> myAirports = new HashSet<Airport>();
		
		// Get the user ID
		int userID = ctx.getUser().getID();
		if ((ctx.isUserInRole("PIREP") || ctx.isUserInRole("HR")) && (ctx.getID() != 0))
			userID = ctx.getID();
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(userID);
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + userID);
			
			ctx.setAttribute("pilot", p, REQUEST);

			// Load airports
			GetFlightReports frdao = new GetFlightReports(con);
			Collection<RoutePair> routes = frdao.getRoutePairs(userID);
			for (RoutePair rp : routes) {
				myAirports.add(rp.getAirportD());
				myAirports.add(rp.getAirportA());
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Get all airports and remove those we've visited or which aren't active
		Map<String, Collection<Airport>> airports = new LinkedHashMap<String, Collection<Airport>>();
		airports.put(defaultCode, new LinkedHashSet<Airport>());
		Collection<Airport> allAirports = new TreeSet<Airport>(SystemData.getAirports().values());
		for (Airport a : allAirports) {
			if (myAirports.contains(a) || a.getAirlineCodes().isEmpty())
				continue;
			
			// Sort the airlines for the airport, default first
			Collection<String> apAirlines = new LinkedHashSet<String>();
			if (a.hasAirlineCode(defaultCode))
				apAirlines.add(defaultCode);
			apAirlines.addAll(a.getAirlineCodes());
			
			// Get the first airline and its group
			String alCode = apAirlines.iterator().next();
			Collection<Airport> aps = airports.get(alCode);
			if (aps == null) {
				aps = new LinkedHashSet<Airport>();
				airports.put(alCode, aps);
			}
			
			aps.add(a);
		}
		
		// Check in case we've visited all airports for the default airline
		Collection<Airport> defaultAirlineList = airports.get(defaultCode);
		if (defaultAirlineList.isEmpty())
			airports.remove(defaultCode);
		
		// Save in request
		ctx.setAttribute("airports", airports, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/missedAirports.jsp");
		result.setSuccess(true);
	}
}