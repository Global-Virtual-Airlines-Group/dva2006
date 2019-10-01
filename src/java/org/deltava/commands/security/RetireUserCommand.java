// Copyright 2010, 2015, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.*;
import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to retire a User. 
 * @author Luke
 * @version 8.7
 * @since 3.0
 */

public class RetireUserCommand extends AbstractCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occrurs.
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the User
			GetPilot dao = new GetPilot(con);
			Pilot usr = dao.get(ctx.getID());
			if (usr == null)
				throw notFoundException("Invalid User ID - " + ctx.getID());
			
			// Check our current status
			PilotAccessControl access = new PilotAccessControl(ctx, usr);
			access.validate();
			if (!access.getCanInactivate())
				throw securityException("Cannot Retire Pilot");
			
			// Save the pilot in the request
			ctx.setAttribute("pilot", usr, REQUEST);
			
			// Update the pilot status
			usr.setStatus(Pilot.RETIRED);
			
			// Log the change
			StatusUpdate upd = new StatusUpdate(usr.getID(), UpdateType.STATUS_CHANGE);
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("User Retired");
			
			// Start the transaction
			ctx.startTX();
			
			// Update the pilot profile
			SetPilot pwdao = new SetPilot(con);
			SetPilotEMail pewdao = new SetPilotEMail(con);
			pwdao.write(usr);
			pewdao.disable(usr.getID());
			
			// Write the status update entry
			SetStatusUpdate sudao = new SetStatusUpdate(con);
			sudao.write(upd);
			
			// Get the authenticator
			Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
			if (auth instanceof SQLAuthenticator) {
				try (SQLAuthenticator sqlAuth = (SQLAuthenticator) auth) {
					sqlAuth.setConnection(con);
					sqlAuth.disable(usr);
				}
			} else
				auth.disable(usr);
			
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Notify other web applications
		ctx.setAttribute("statusUpdated", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pilotUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}