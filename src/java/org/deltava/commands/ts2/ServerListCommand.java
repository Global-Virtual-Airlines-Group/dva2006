// Copyright 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.ts2;

import org.deltava.beans.ts2.Server;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display TeamSpeak 2 server profiles.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class ServerListCommand extends AbstractViewCommand {

	/**
	 * Executes the Command.
	 * @param ctx the Command Context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<Server> vc = initView(ctx, Server.class);
		try {
			GetTS2Data dao = new GetTS2Data(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
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