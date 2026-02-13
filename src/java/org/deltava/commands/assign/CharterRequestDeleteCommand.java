// Copyright 2021, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.sql.Connection;

import org.deltava.beans.assign.CharterRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CharterRequestAccessControl;

/**
 * A Web Site Comand to delete Charter flight Requests. 
 * @author Luke
 * @version 12.4.
 * @since 10.0
 */

public class CharterRequestDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the request
			GetCharterRequests rqdao = new GetCharterRequests(con);
			CharterRequest req = rqdao.get(ctx.getID());
			if (req == null)
				throw notFoundException("Invalid Charter Request", ctx.getID());
			
			// Check access
			CharterRequestAccessControl ac = new CharterRequestAccessControl(ctx, req);
			ac.validate();
			if (!ac.getCanEdit())
				throw securityException("Cannot delete Charter Request " + ctx.getID());
			
			// Delete
			SetAssignment rwdao = new SetAssignment(con);
			rwdao.delete(req);
			ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/assign/charterRequestUpdate.jsp");
		result.setSuccess(true);
	}
}