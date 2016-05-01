// Copyright 2006, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.mvs;

import org.deltava.beans.mvs.Channel;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display MVS persistent channels.
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

		// Init the view context
		ViewContext<Channel> vc = initView(ctx, Channel.class);
		try {
			GetMVSChannel dao = new GetMVSChannel(ctx.getConnection());
			dao.setQueryMax(vc.getCount());
			dao.setQueryStart(vc.getStart());
			vc.setResults(dao.getAll());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/mvs/channelList.jsp");
		result.setSuccess(true);
	}
}