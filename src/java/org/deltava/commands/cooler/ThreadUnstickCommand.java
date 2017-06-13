// Copyright 2005, 2006, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.cooler.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

/**
 * A Web Site Command to unstick Water Cooler Threads.
 * @author Luke
 * @version 7.4
 * @since 1.0
 */

public class ThreadUnstickCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		MessageThread mt = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the thread
			GetCoolerThreads dao = new GetCoolerThreads(con);
			mt = dao.getThread(ctx.getID());
			if (mt == null)
				throw notFoundException("Invalid Message Thread -" + ctx.getID());
			
			// Get the Channel
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel c = cdao.get(mt.getChannel());
			if (c == null)
				throw notFoundException("Invalid Channel - " + mt.getChannel());
			
			// Check our access
			CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
			access.updateContext(mt, c);
			access.validate();
			if (!access.getCanUnstick())
				throw securityException("Cannot unstick Thread " + mt.getID());
			
			// Create the status update bean
			ThreadUpdate upd = new ThreadUpdate(mt.getID());
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("Message Thread unstuck");
			
			// Start a transaction
			ctx.startTX();
			
			// Unstick the thread
			SetCoolerMessage wdao = new SetCoolerMessage(con);
			wdao.unstickThread(mt.getID());
			wdao.write(upd);
			
            // Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set message thread and status attribute
		ctx.setAttribute("thread", mt, REQUEST);
		ctx.setAttribute("isUnstuck", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/cooler/threadUpdate.jsp");
		result.setSuccess(true);
	}
}