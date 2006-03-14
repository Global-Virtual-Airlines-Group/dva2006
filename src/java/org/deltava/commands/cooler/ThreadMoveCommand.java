// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.cooler.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

/**
 * A Web Site Command to move a Message Thread to another Water Cooler channel.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ThreadMoveCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the Thread we wish to move
			GetCoolerThreads trdao = new GetCoolerThreads(con);
			MessageThread t = trdao.getThread(ctx.getID());
			if (t == null)
				throw notFoundException("Invalid Message Thread - " + ctx.getID());
			
			// Get the Channel we wish to move to
			String newChannel = ctx.getParameter("newChannel");
			GetCoolerChannels crdao = new GetCoolerChannels(con);
			Channel ch = crdao.get(newChannel);
			if (ch == null)
				throw notFoundException("Invalid Channel - " + newChannel);
			
			// Check our access to the new channel - if we cannot read the new channel, then abort
			CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
			access.updateContext(t, ch);
			access.validate();
			if (!access.getCanRead())
				throw securityException("Cannot move Thread to " + newChannel);
			
			// Save thread and set new channel
			ctx.setAttribute("isMoved", Boolean.TRUE, REQUEST);
			ctx.setAttribute("newChannel", newChannel, REQUEST);
			ctx.setAttribute("thread", t, REQUEST);
			
			// Update the thread
			SetCoolerMessage twdao = new SetCoolerMessage(con);
			twdao.setChannel(ctx.getID(), newChannel);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/cooler/threadUpdate.jsp");
		result.setSuccess(true);
	}
}