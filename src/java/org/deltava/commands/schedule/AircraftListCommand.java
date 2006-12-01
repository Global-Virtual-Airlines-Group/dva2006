// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display aircraft data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AircraftListCommand extends AbstractCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the aircraft
			GetAircraft dao = new GetAircraft(con);
			ctx.setAttribute("aircraftInfo", dao.getAll(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/aircraftList.jsp");
		result.setSuccess(true);
	}
}