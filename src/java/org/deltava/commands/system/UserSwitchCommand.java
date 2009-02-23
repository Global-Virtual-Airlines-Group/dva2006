// Copyright 2005, 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to switch credentials and impersonate a user.
 * @author Luke
 * @version 2.4
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
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());
			
			// Populate online totals
			if (usr.getACARSLegs() < 0) {
				GetFlightReports frdao = new GetFlightReports(con);
				frdao.getOnlineTotals(usr, SystemData.get("airline.db"));
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Log warning
		log.warn(ctx.getUser().getName() + " switching to user " + usr.getName());
		
		// Save new user in request and switch to the user, saving the old user
		ctx.setAttribute("user", usr, REQUEST);
		ctx.setAttribute(HTTPContext.SU_ATTR_NAME, ctx.getUser(), SESSION);
		ctx.setAttribute(HTTPContext.USER_ATTR_NAME, usr, SESSION);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/userSwitch.jsp");
		result.setSuccess(true);
	}
}