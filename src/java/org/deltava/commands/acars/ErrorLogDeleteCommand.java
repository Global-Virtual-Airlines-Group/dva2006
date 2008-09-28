// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to delete an ACARS client error report.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ErrorLogDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and delete the entry
			SetACARSLog dao = new SetACARSLog(con);
			dao.deleteError(ctx.getID());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("errorDelete", Boolean.TRUE, REQUEST);

		// Forward to the error list
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/acars/logEntryDelete.jsp");
		result.setSuccess(true);
	}
}