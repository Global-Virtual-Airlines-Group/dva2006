// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.sql.Connection;

import org.deltava.beans.assign.AssignmentInfo;

import org.deltava.commands.*;

import org.deltava.dao.GetAssignment;
import org.deltava.dao.SetAssignment;
import org.deltava.dao.DAOException;

import org.deltava.security.command.AssignmentAccessControl;

/**
 * A Web Site Command to delete Flight Assignments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AssignmentDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Assignment
			GetAssignment dao = new GetAssignment(con);
			AssignmentInfo assign = dao.get(ctx.getID());
			if (assign == null)
				throw notFoundException("Invalid Flight Assignment - " + ctx.getID());

			// Calculate our access
			AssignmentAccessControl access = new AssignmentAccessControl(ctx, assign);
			access.validate();
			if (!access.getCanDelete())
				throw securityException("Cannot delete Flight Assignment " + ctx.getID());

			// Delete the assignment
			SetAssignment wdao = new SetAssignment(con);
			wdao.delete(assign);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Update status for JSP
		ctx.setAttribute("pilot", ctx.getUser(), REQUEST);
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/assign/assignUpdate.jsp");
		result.setType(CommandResult.REQREDIRECT);
		result.setSuccess(true);
	}
}