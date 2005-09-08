// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.system;

import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to switch credentials and impersonate a user.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class UserSwitchCommand extends AbstractCommand {

	private static final Logger log = Logger.getLogger(UserSwitchCommand.class);
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Double-check access - this is a really powerful command
		if (!ctx.isUserInRole("Admin"))
			throw securityException("Cannot switch Users");
		
		Pilot usr = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the User
			GetPilot dao = new GetPilot(con);
			usr = dao.get(ctx.getID());
			if (usr == null)
				throw new CommandException("Invalid Pilot ID - " + ctx.getID());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Log warning
		log.warn(ctx.getUser().getName() + " switching to user " + usr.getName());
		
		// Save new user in request
		ctx.setAttribute("user", usr, REQUEST);
		
		// Switch to the user, saving the old user
		ctx.setAttribute(CommandContext.SU_ATTR_NAME, ctx.getUser(), SESSION);
		ctx.setAttribute(CommandContext.USER_ATTR_NAME, usr, SESSION);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/userSwitch.jsp");
		result.setSuccess(true);
	}
}