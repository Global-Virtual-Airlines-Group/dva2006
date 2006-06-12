// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to detect airports no longer serviced by an Airline.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class UnservicedAirportsCommand extends AbstractCommand {
	
	private class AirlineFilter implements AirportFilter {

		private Airline _a;

		AirlineFilter(Airline a) {
			super();
			_a = (a == null) ? SystemData.getAirline(SystemData.get("airline.code")) : a;
		}

		public boolean accept(Airport a) {
			return (a == null) ? false : a.getAirlineCodes().contains(_a.getCode());
		}
	}

	/**
	 * Helper method to return airports for a particular airline.
	 */
	private Collection<Airport> getAirports(Airline a) {
		AirlineFilter filter = new AirlineFilter(a);
		Collection<Airport> allAirports = new LinkedHashSet<Airport>(SystemData.getAirports().values());
		for (Iterator<Airport> i = allAirports.iterator(); i.hasNext(); ) {
			Airport ap = i.next();
			if (!filter.accept(ap))
				i.remove();
		}
		
		return allAirports;
	}
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the airline list
		Collection<Airline> airlines = SystemData.getAirlines().values(); 
		
		int totalResults = 0;
		Map<Airline, Collection<Airport>> results = new TreeMap<Airline, Collection<Airport>>();
		try {
			Connection con = ctx.getConnection();
			
			// Loop through the airlines
			GetScheduleAirport dao = new GetScheduleAirport(con);
			for (Iterator<Airline> i = airlines.iterator(); i.hasNext(); ) {
				Airline a = i.next();
				
				// Detrmine which airports we do not have schedule entries for
				Collection<Airport> extraAirports = CollectionUtils.getDelta(getAirports(a), dao.getOriginAirports(a));
				if (!extraAirports.isEmpty()) {
					results.put(a, extraAirports);
					totalResults += extraAirports.size();
				}
			}
		} catch (DAOException de) { 
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save the airports
		ctx.setAttribute("results", results, REQUEST);
		ctx.setAttribute("airlines", results.keySet(), REQUEST);
		ctx.setAttribute("totalResults", new Integer(totalResults), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/unsvcAirports.jsp");
		result.setSuccess(true);
	}
}