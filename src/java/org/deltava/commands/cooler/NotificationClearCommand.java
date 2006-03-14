// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.cooler.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

/**
 * A Web Site Command for clearing Water Cooler notifications.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NotificationClearCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the thread ID
		int threadID = ctx.getID();
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the thread
			GetCoolerThreads tdao = new GetCoolerThreads(con);
			MessageThread mt = tdao.getThread(threadID);
			if (mt == null)
				throw notFoundException("Invalid Message Thread - " + threadID);
			
			// Get the DAO and the channel
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel c = cdao.get(mt.getChannel());
			if (c == null)
				throw notFoundException("Invalid Channel - " + mt.getChannel());
			
			// Validate our access to the thread
			CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
			access.updateContext(mt, c);
			access.validate();
			if (!access.getCanResync())
				throw securityException("Cannot clear notifications");
			
			// Get the write DAO and clear the notifications
			SetCoolerNotification wdao = new SetCoolerNotification(con);
			wdao.clear(threadID);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward back to the thread
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setURL("thread", null, threadID);
		result.setSuccess(true);
	}
}