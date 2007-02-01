// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.StatusUpdate;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.Authenticator;
import org.deltava.security.SQLAuthenticator;
import org.deltava.security.UserPool;
import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to lock out a user.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SuspendUserCommand extends AbstractCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occrurs.
	 */
	public void execute(CommandContext ctx) throws CommandException {

		Pilot usr = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the User
			GetPilot dao = new GetPilot(con);
			usr = dao.get(ctx.getID());
			if (usr == null)
				throw notFoundException("Invalid User ID - " + ctx.getID());
			
			// Check our current status
			PilotAccessControl access = new PilotAccessControl(ctx, usr);
			access.validate();
			if (!access.getCanSuspend())
				throw securityException("Cannot suspend Pilot");
				
			// Update the pilot status
			usr.setStatus(Pilot.SUSPENDED);
			
			// Log the change
			StatusUpdate upd = new StatusUpdate(usr.getID(), StatusUpdate.STATUS_CHANGE);
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("User Account Suspended");
			
			// Start the transaction
			ctx.startTX();
			
			// Update the pilot profile
			SetPilot pwdao = new SetPilot(con);
			pwdao.write(usr);
			
			// Write the status update entry
			SetStatusUpdate sudao = new SetStatusUpdate(con);
			sudao.write(upd);
			
			// Get the authenticator
			Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
			if (auth instanceof SQLAuthenticator) {
				SQLAuthenticator sqlAuth = (SQLAuthenticator) auth;
				sqlAuth.setConnection(con);
				sqlAuth.removeUser(usr);
				sqlAuth.clearConnection();
			} else
				auth.removeUser(usr);
			
			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save the pilot in the request
		ctx.setAttribute("pilot", usr, REQUEST);
		
		// Block the user
		UserPool.block(usr);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pilotUpdate.jsp");
		result.setType(CommandResult.REQREDIRECT);
		result.setSuccess(true);
	}
}