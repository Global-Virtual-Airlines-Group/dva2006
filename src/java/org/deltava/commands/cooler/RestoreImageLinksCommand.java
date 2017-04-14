// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.cooler.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

/**
 * A Web Site Command to restore disabled Image Links.
 * @author Luke
 * @version 7.3
 * @since 7.3
 */

public class RestoreImageLinksCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Load the thread
			GetCoolerThreads mtdao = new GetCoolerThreads(con);
			MessageThread mt = mtdao.getThread(ctx.getID());
			if (mt == null)
				throw notFoundException("Invalid Message Thread - " + ctx.getID());
			
			// Get the channel
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel ch = cdao.get(mt.getChannel());
			
			// Check our access
			CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
			access.updateContext(mt, ch);
			if (!access.getCanUnlinkImage())
				throw securityException("Cannot restore Linked Images to Message Thread " + ctx.getID());
			
			// Ensure we have disabled linked images
			if (!mt.getHasDisabledLinks())
				throw notFoundException("No disabled Image Links on Message Thread " + ctx.getID());
			
			// Restore the images
			SetCoolerLinks wdao = new SetCoolerLinks(con);
			wdao.restore(mt.getID());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
			
		// Return to the thread
		CommandResult result = ctx.getResult();
		result.setURL("thread", null, ctx.getID());
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}