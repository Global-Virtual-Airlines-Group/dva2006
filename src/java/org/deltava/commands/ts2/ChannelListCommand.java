// Copyright (c) 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.ts2;

import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;

/**
 * A Web Site Command to display TeamSpeak 2 channels.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ChannelListCommand extends AbstractViewCommand {

	/**
	 * Executes the Command.
	 * @param ctx the Command Context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Init the view context
		ViewContext vc = initView(ctx);

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the servers
			GetTS2Data dao = new GetTS2Data(con);
			ctx.setAttribute("servers", CollectionUtils.createMap(dao.getServers(), "ID"), REQUEST);
			
			// Get the channels
			dao.setQueryMax(vc.getCount());
			dao.setQueryStart(vc.getStart());
			vc.setResults(dao.getChannels());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/ts2/channelList.jsp");
		result.setSuccess(true);
	}
}