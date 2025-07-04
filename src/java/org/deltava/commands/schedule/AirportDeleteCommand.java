// Copyright 2005, 2006, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;

import org.deltava.beans.schedule.Airport;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to delete Airport profiles.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public class AirportDeleteCommand extends AbstractCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Airport
			GetAirport dao = new GetAirport(con);
			Airport a = dao.get((String) ctx.getCmdParameter(Command.ID, null));
			if (a == null)
				throw notFoundException("Unknown Airport - " + ctx.getCmdParameter(Command.ID, null));
			
			// Get the write DAO and delete the airport
			SetAirportAirline wdao = new SetAirportAirline(con);
			wdao.delete(a);
			
			// Save the airport in the request
			ctx.setAttribute("airport", a, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status for the JSP
		ctx.setAttribute("isAirport", Boolean.TRUE, REQUEST);
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/schedule/scheduleUpdate.jsp");
		result.setSuccess(true);
	}
}