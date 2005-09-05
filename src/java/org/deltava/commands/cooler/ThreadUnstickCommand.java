// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.cooler.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

/**
 * A Web Site Command to unstick Water Cooler Threads.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ThreadUnstickCommand extends AbstractCommand {

	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the thread
			GetCoolerThreads dao = new GetCoolerThreads(con);
			MessageThread mt = dao.getThread(ctx.getID());
			if (mt == null)
				throw new CommandException("Invalid Message Thread -" + ctx.getID());
			
			// Get the Channel
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel c = cdao.get(mt.getChannel());
			if (c == null)
				throw new CommandException("Invalid Channel - " + mt.getChannel());
			
			// Check our access
			CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
			access.updateContext(mt, c);
			access.validate();
			if (!access.getCanUnstick())
				throw securityException("Cannot unstick Thread " + mt.getID());
			
			// Unstick the thread
			SetCoolerMessage wdao = new SetCoolerMessage(con);
			wdao.unstickThread(mt.getID());
			
			// Save the message thread
			ctx.setAttribute("thread", mt, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isUnstuck", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/cooler/threadUpdate.jsp");
		result.setSuccess(true);
	}
}