// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.sql.Connection;

import org.deltava.beans.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to save Status History commands for a Pilot.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class StatusCommentCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get command result
		CommandResult result = ctx.getResult();

		Pilot usr = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Pilot
			GetPilot pdao = new GetPilot(con);
			usr = pdao.get(ctx.getID());
			if (usr == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());
			
			// Check our access
			PilotAccessControl access = new PilotAccessControl(ctx, usr);
			access.validate();
			if (!access.getCanChangeStatus() && !access.getCanEdit())
				throw securityException("Cannot comment on " + usr.getName());
			
			// Save the user
			ctx.setAttribute("pilot", usr, REQUEST);

			// Load the status update entries
			GetStatusUpdate sudao = new GetStatusUpdate(con);
			ctx.setAttribute("statusUpdates", sudao.getByUser(usr.getID(), SystemData.get("airline.db")), REQUEST);
			
			// If we're doing a GET, forward to the JSP
			if (ctx.getParameter("comment") == null) {
				ctx.release();
				result.setURL("/jsp/pilot/statusComment.jsp");
				result.setSuccess(true);
				return;
			}

			// Create a new comment
			StatusUpdate upd = new StatusUpdate(usr.getID(), StatusUpdate.COMMENT);
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription(ctx.getParameter("comment"));
			
			// Write the Command
			SetStatusUpdate wdao = new SetStatusUpdate(con);
			wdao.write(upd);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward back to the Profile
		result.setType(CommandResult.REDIRECT);
		result.setURL("profile", null, usr.getID());
		result.setSuccess(true);
	}
}