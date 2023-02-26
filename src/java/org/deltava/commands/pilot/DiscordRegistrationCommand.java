// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to associate Pilots with a Discord user ID.
 * @author Luke
 * @version 10.5
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
		
		String uuid = ctx.getParameter("id");
		if (StringUtils.isEmpty(uuid))
			throw notFoundException("No UUID present");

		// Check our access - if we cannot get in, go directly to login page
		CommandResult result = ctx.getResult();
		if (!ctx.isAuthenticated()) {
			ctx.setAttribute("referTo", String.format("%s?id=%s", ctx.getRequest().getRequestURI(), uuid), REQUEST);
			CommandException ce = securityException("Not Authenticated");
			ce.setForwardURL("/jsp/login.jsp");
			throw ce;
		} 
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getUser().getID());
			boolean isNew = p.hasID(ExternalID.DISCORD);

			// Update the discord ID
			p.setExternalID(ExternalID.DISCORD, uuid);
			ctx.setAttribute("discordRegister", Boolean.TRUE, REQUEST);
			ctx.setAttribute("discordID", uuid, REQUEST);
			ctx.setAttribute("pilot", p, REQUEST);
			
			// Create status update
			StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.EXT_AUTH);
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription(String.format("Discord Integration %s", isNew ? "created" : "updated"));
			
			// Start transaction
			ctx.startTX();
			
			// Save the pilot/update
			SetPilot pwdao = new SetPilot(con);
			SetStatusUpdate uwdao = new SetStatusUpdate(con);
			pwdao.write(p, ctx.getDB());
			uwdao.write(upd, ctx.getDB());
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
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