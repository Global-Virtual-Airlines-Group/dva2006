// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to delete Facebook credential data. 
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public class FacebookClearCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occrurs.
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			// Load the user
			Connection con = ctx.getConnection();
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getUser().getID());
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getUser().getID());
			
			// Clear FB credentials
			boolean isUpdate = p.hasIM(IMAddress.FB) || p.hasIM(IMAddress.FBTOKEN);
			if (isUpdate) {
				p.setIMHandle(IMAddress.FB, null);
				p.setIMHandle(IMAddress.FBTOKEN, null);
				
				// Update pilot
				SetPilot pwdao = new SetPilot(con);
				pwdao.write(p);
			}
			
			// Set status variables
			ctx.setAttribute("fbDeauth", Boolean.TRUE, REQUEST);
			ctx.setAttribute("isFBDelete", Boolean.valueOf(isUpdate), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forwad to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/fbAuth.jsp");
		result.setSuccess(true);
	}
}