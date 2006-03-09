// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.cooler.MessageThread;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to override the Profanity Filter for all posts in a Water Cooler message thread.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ContentOverrideCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the thread
			GetCoolerThreads dao = new GetCoolerThreads(con);
			MessageThread mt = dao.getThread(ctx.getID());
			if (mt ==  null)
				throw notFoundException("Invalid Thread - " + ctx.getID());
			
			// Clean out the thread
			SetCoolerMessage wdao = new SetCoolerMessage(con);
			wdao.clearWarning(mt.getID());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the thread
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("thread", null, ctx.getID());
		result.setSuccess(true);
	}
}