// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.sql.Connection;

import org.deltava.beans.econ.EliteLifetime;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to delete lifetime Elite status levels.
 * @author Luke
 * @version 11.5
 * @since 11.5
 */

public class EliteLifetimeDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			GetElite edao = new GetElite(con);
			EliteLifetime el = edao.getLifetime(ctx.getParameter("id"), ctx.getDB());
			if (el == null)
				throw notFoundException("Invalid lifetime ELite level - " + ctx.getParameter("id"));
			
			// Delete the level
			SetElite ewdao = new SetElite(con);
			ewdao.delete(el.getCode());
			
			// Save status attributes
			ctx.setAttribute("ltLevel", el, REQUEST);
			ctx.setAttribute("isDeleteLT", Boolean.TRUE, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/econ/eliteLevelUpdate.jsp");
		result.setSuccess(true);
	}
}