// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to associate Pilots with a Discord user ID.
 * @author Luke
 * @version 10.4
 * @since 10.4
 */

public class DiscordRegistrationCommand extends AbstractCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occrurs.
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check our access - if we cannot get in, go directly to login page
		CommandResult result = ctx.getResult();
		if (!ctx.isAuthenticated()) {
			ctx.setAttribute("referTo", ctx.getRequest().getRequestURI(), REQUEST);
			CommandException ce = securityException("Not Authenticated");
			ce.setForwardURL("/jsp/login.jsp");
			throw ce;
		}
		
		String uuid = ctx.getParameter("id"); 
		try {
			Connection con = ctx.getConnection();
			
			// Get the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getUser().getID());

			// Update the discord ID
			p.setExternalID(ExternalID.DISCORD, uuid);
			ctx.setAttribute("discordRegister", Boolean.TRUE, REQUEST);
			ctx.setAttribute("discordID", uuid, REQUEST);
			ctx.setAttribute("pilot", p, REQUEST);
			
			// Save the pilot
			SetPilot pwdao = new SetPilot(con);
			pwdao.write(p, ctx.getDB());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setURL("/jsp/pilot/pilotUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}