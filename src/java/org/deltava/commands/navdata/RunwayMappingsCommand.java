// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Simulator;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display runway mappings.
 * @author Luke
 * @version 8.3
 * @since 8.3
 */

public class RunwayMappingsCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Load the airports
			GetRunwayMapping rmdao = new GetRunwayMapping(con);
			SortedSet<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
			airports.addAll(rmdao.getAirports());
			Map<String, Airport> allAirports = new HashMap<String, Airport>();
			airports.forEach(a -> allAirports.put(a.getICAO(), a));

			// Save in request
			ctx.setAttribute("airports", airports, REQUEST);
			ctx.setAttribute("allAirports", allAirports, REQUEST);
			
			// Get the airport
			Airport a = SystemData.getAirport(ctx.getParameter("id"));
			if ((a == null) && !airports.isEmpty())
				a = airports.first();
			
			// Load the mappings
			if (a != null) {
				ViewContext<RunwayMapping> vctx = initView(ctx, RunwayMapping.class);
				vctx.setResults(rmdao.getAll(a));
				ctx.setAttribute("airport", a, REQUEST);
				
				// Load runway info
				Map<String, Runway> rwys = new HashMap<String, Runway>();
				GetNavData navdao = new GetNavData(con);
				for (RunwayMapping rm : vctx.getResults()) {
					Runway r = navdao.getRunway(a, rm.getNewCode(), Simulator.UNKNOWN);
					if (r != null)
						rwys.put(r.getCode(), r);
				}
				
				ctx.setAttribute("runways", rwys, REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/navdata/rwyMappings.jsp");
		result.setSuccess(true);
	}
}