// Copyright 2006, 2009, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.util.stream.Collectors;
import java.net.*;
import java.sql.Connection;

import org.deltava.beans.cooler.MessageThread;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to clear a user's Water Cooler thread unread marks. 
 * @author Luke
 * @version 6.0
 * @since 1.0
 */

public class UnreadClearCommand extends AbstractCommand {
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			GetCoolerThreads mtdao = new GetCoolerThreads(con);
			List<MessageThread> threads = mtdao.getSince(ctx.getUser().getLastLogoff(), true);
			threads.addAll(mtdao.getSince(CalendarUtils.adjust(null, -60), true));
			Collection<Integer> threadIDs = threads.stream().map(mt -> Integer.valueOf(mt.getID())).collect(Collectors.toSet());

			// Mark as read
			ctx.startTX();
			SetCoolerMessage lrdao = new SetCoolerMessage(con);
			for (Integer id : threadIDs)
				lrdao.markRead(id.intValue(), ctx.getUser().getID());

			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Determine where we are referring from, if on the site return back there
		CommandResult result = ctx.getResult();
		String referer = ctx.getRequest().getHeader("Referer");
		if (!StringUtils.isEmpty(referer) && (!referer.contains("login"))) {
			try {
				URL url = new URL(referer);
				if (SystemData.get("airline.url").equalsIgnoreCase(url.getHost())) {
					long now = System.currentTimeMillis() / 1000;
					if (referer.indexOf('?') > 0)
						result.setURL(referer + "&noCache=" + now);
					else
						result.setURL(referer + "?noCache=" + now);
				}
			} catch (MalformedURLException mue) {
				// empty
			}
		}
		
		// Return to the previous page
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}