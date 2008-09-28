// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.cooler.*;
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
			
			// Create the status update bean
			ThreadUpdate upd = new ThreadUpdate(mt.getID());
			upd.setAuthorID(ctx.getUser().getID());
			upd.setMessage("Content Warnings/Reports cleared");
			
			// Start a transaction
			ctx.startTX();
			
			// Clean out the thread and log the status
			SetCoolerMessage wdao = new SetCoolerMessage(con);
			wdao.clearWarning(mt.getID());
			wdao.clearReport(mt.getID());
			wdao.write(upd);
			
			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the thread
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("thread", null, ctx.getID());
		result.setSuccess(true);
	}
}