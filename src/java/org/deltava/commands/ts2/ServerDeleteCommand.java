// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.ts2;

import java.sql.Connection;

import org.deltava.beans.ts2.Server;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to delete a TeamSpeak 2 virtual server.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ServerDeleteCommand extends AbstractCommand {

	/**
	 * Executes the Command.
	 * @param ctx the Command Context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the server
			GetTS2Data dao = new GetTS2Data(con);
			Server srv = dao.getServer(ctx.getID());
			if (srv == null)
				throw notFoundException("Invalid Server ID - " + ctx.getID());
			
			// Get the write DAO and delete the server
			SetTS2Data wdao = new SetTS2Data(con);
			wdao.delete(srv);
			
			// Save the server in the request
			ctx.setAttribute("server", srv, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/ts2/ts2Update.jsp");
		result.setSuccess(true);
	}
}