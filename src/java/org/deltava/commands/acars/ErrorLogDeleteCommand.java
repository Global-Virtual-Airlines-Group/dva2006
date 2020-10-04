// Copyright 2006, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.sql.Connection;

import org.deltava.beans.acars.ACARSError;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.security.command.ErrorLogAccessControl;

/**
 * A Web Site Command to delete an ACARS client error report.
 * @author Luke
 * @version 9.1
 * @since 1.0
 */

public class ErrorLogDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Check that it exists
			GetACARSErrors dao = new GetACARSErrors(con);
			ACARSError err = dao.get(ctx.getID());
			if (err == null)
				throw notFoundException("Invalid Error Report - " + ctx.getID());
			
			// Check our access
			ErrorLogAccessControl ac = new ErrorLogAccessControl(ctx, err);
			ac.validate();
			if (!ac.getCanDelete())
				throw securityException("Cannot delete Error Report " + err.getID());
			
			// Get the DAO and delete the entry
			SetACARSLog wdao = new SetACARSLog(con);
			wdao.deleteError(ctx.getID());
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