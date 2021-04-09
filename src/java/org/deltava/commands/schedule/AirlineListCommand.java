// Copyright 2005, 2006, 2012, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.Airline;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display Airline profiles. 
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class AirlineListCommand extends AbstractCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the airline list
			GetAirline dao = new GetAirline(con);
			Map<String, Airline> airlines = dao.getAll();
			ctx.setAttribute("airlines", new TreeSet<Airline>(airlines.values()), REQUEST);
			
			// Get airport counts
			GetAirport apdao = new GetAirport(con);
			ctx.setAttribute("apCount", apdao.getAirportCounts(), REQUEST);
			
			// Get flight counts
			GetScheduleInfo sdao = new GetScheduleInfo(con);
			ctx.setAttribute("fCount", sdao.getAirlineCounts(ctx.getDB()), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/airlineList.jsp");
		result.setSuccess(true);
	}
}