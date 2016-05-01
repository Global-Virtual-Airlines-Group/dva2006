// Copyright 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.ts2;

import org.deltava.beans.ts2.Channel;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;

/**
 * A Web Site Command to display TeamSpeak 2 channels.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class ChannelListCommand extends AbstractViewCommand {

	/**
	 * Executes the Command.
	 * @param ctx the Command Context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		ViewContext<Channel> vc = initView(ctx, Channel.class);
		try {
			// Get the DAO and the servers
			GetTS2Data dao = new GetTS2Data(ctx.getConnection());
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