// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.util.*;

import org.deltava.beans.navdata.RunwayMapping;
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
			// Load the airports
			GetRunwayMapping rmdao = new GetRunwayMapping(ctx.getConnection());
			SortedSet<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
			airports.addAll(airports);
			ctx.setAttribute("airports", airports, REQUEST);
			
			// Get the airport
			Airport a = SystemData.getAirport(ctx.getParameter("id"));
			if ((a == null) && !airports.isEmpty())
				a = airports.first();
			
			// Load the mappings
			if (a != null) {
				ViewContext<RunwayMapping> vctx = initView(ctx, RunwayMapping.class);
				vctx.setResults(rmdao.getAll(a));
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/navdata/runwayMappings.jsp");
		result.setSuccess(true);
	}
}