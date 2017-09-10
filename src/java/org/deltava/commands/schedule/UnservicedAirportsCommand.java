// Copyright 2006, 2008, 2009, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.filter.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to detect airports no longer serviced by an Airline.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public class UnservicedAirportsCommand extends AbstractCommand {
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the airline list and determine if we write to the database
		Collection<Airline> airlines = SystemData.getAirlines().values(); 
		boolean updateDB = Boolean.valueOf(ctx.getParameter("updateDB")).booleanValue();
		
		int totalResults = 0;
		Collection<Airport> updateAirports = new HashSet<Airport>();
		Map<Airline, Collection<Airport>> results = new TreeMap<Airline, Collection<Airport>>();
		try {
			Connection con = ctx.getConnection();
			
			// Loop through the airlines
			GetScheduleAirport dao = new GetScheduleAirport(con);
			for (Airline a : airlines) {
				AirlineFilter filter = new AirlineFilter(a);
				Collection<Airport> aa = filter.filter(SystemData.getAirports().values());
				
				// Detrmine which airports we do not have schedule entries for
				Collection<Airport> extraAirports = CollectionUtils.getDelta(aa, dao.getOriginAirports(a));
				if (!extraAirports.isEmpty()) {
					results.put(a, extraAirports);
					totalResults += extraAirports.size();
					extraAirports.forEach(ap -> { ap.removeAirlineCode(a.getCode()); updateAirports.add(ap); });
				}
			}
			
			// Update the database if required
			if (updateDB) {
				ctx.startTX();
				
				// Get the DAO and update the airports
				SetAirportAirline wdao = new SetAirportAirline(con);
				for (Airport ap : updateAirports)
					wdao.update(ap, ap.getIATA());
				
				ctx.commitTX();
				
				// Reload the database
				GetAirport apdao = new GetAirport(con);
				SystemData.add("airports", apdao.getAll());
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save the airports
		ctx.setAttribute("results", results, REQUEST);
		ctx.setAttribute("totalResults", Integer.valueOf(totalResults), REQUEST);
		ctx.setAttribute("updateDB", Boolean.valueOf(updateDB), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/unsvcAirports.jsp");
		result.setSuccess(true);
	}
}