// Copyright (c) 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.ts2;

import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetTS2Data;
import org.deltava.dao.DAOException;

/**
 * A Web Site Command to display TeamSpeak 2 server profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ServerListCommand extends AbstractViewCommand {

	/**
	 * Executes the Command.
	 * @param ctx the Command Context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the view Context
		ViewContext vc = initView(ctx);

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the servers
			GetTS2Data dao = new GetTS2Data(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			
			// Execute the query
			vc.setResults(dao.getServers());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/ts2/serverList.jsp");
		result.setSuccess(true);
	}
}