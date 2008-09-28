// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.*;
import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.system.SystemData;

import org.gvagroup.common.*;

/**
 * A Web Site Command to lock out a user.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class SuspendUserCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(SuspendUserCommand.class);

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
				sqlAuth.disable(usr);
				sqlAuth.clearConnection();
			} else
				auth.disable(usr);
			
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
		log.warn(ctx.getUser().getName() + " suspended user " + usr.getName());
		
		// Notify other web applications
		EventDispatcher.send(UserEvent.UserSuspend(usr.getID()));
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pilotUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}