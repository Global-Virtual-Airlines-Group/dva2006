// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.cooler.Channel;
import org.deltava.beans.cooler.MessageThread;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

/**
 * A Web Site Command to resynchronize Water Cooler Thread data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ThreadSyncCommand extends AbstractCommand {

	/* (non-Javadoc)
	 * @see org.deltava.commands.Command#execute(org.deltava.commands.CommandContext)
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Message Thread
			GetCoolerThreads dao = new GetCoolerThreads(con);
			MessageThread mt = dao.getThread(ctx.getID());
			if (mt == null)
				throw new CommandException("Invalid Thread - " + ctx.getID());
			
			// Get the DAO and the Channel
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel c = cdao.get(mt.getChannel());
			if (c == null)
				throw new CommandException("Invalid Channel - " + ctx.getID());
			
			// Check our access
			CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
			access.updateContext(mt, c);
			access.validate();
			if (!access.getCanResync())
				throw securityException("Cannot resync Message Thread data");
			
			// Resync the thread
			SetCoolerMessage wdao = new SetCoolerMessage(con);
			wdao.synchThread(mt);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Hide the resync button
		ctx.setAttribute("noResync", Boolean.valueOf(true), REQUEST);

		// Forward back to the thread
		CommandResult result = ctx.getResult();
		result.setURL("thread", null, ctx.getID());
		result.setSuccess(true);
	}
}