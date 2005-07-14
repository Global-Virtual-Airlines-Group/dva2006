// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;


import org.deltava.commands.*;

import org.deltava.dao.GetAirline;
import org.deltava.dao.DAOException;

/**
 * A Web Site Command to display Airline codes. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirlineListCommand extends AbstractCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the airline list
			GetAirline dao = new GetAirline(con);
			List airlines = new ArrayList(dao.getAll().values());
			
			// Sort the airlines and save them
			Collections.sort(airlines);
			ctx.setAttribute("airlines", airlines, REQUEST);
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