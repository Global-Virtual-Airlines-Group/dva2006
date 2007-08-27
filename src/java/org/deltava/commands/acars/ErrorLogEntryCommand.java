// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.sql.Connection;

import org.deltava.beans.UserData;
import org.deltava.beans.acars.ACARSError;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display an ACARS client error report.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ErrorLogEntryCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the entry
			GetACARSErrors dao = new GetACARSErrors(con);
			ACARSError err = dao.get(ctx.getID());
			if (err == null)
				throw notFoundException("Invalid Error Report - " + ctx.getID());
			
			// Load the author
			GetPilot pdao = new GetPilot(con);
			GetUserData uddao = new GetUserData(con);
			UserData ud = uddao.get(err.getUserID());
			ctx.setAttribute("author", pdao.get(ud), REQUEST);
			
			// Save the error report
			ctx.setAttribute("err", err, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the error list
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/acars/errorReport.jsp");
		result.setSuccess(true);
	}
}