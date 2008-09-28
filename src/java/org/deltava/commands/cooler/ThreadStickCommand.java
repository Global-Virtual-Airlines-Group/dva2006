// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.Date;
import java.sql.Connection;

import org.deltava.beans.cooler.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update the sticky date of a Water Cooler thread.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class ThreadStickCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the sticky date
		Date stickyDate = null;
		try {
			stickyDate = parseDateTime(ctx, "sticky", SystemData.get("time.date_format"), "HH:mm");
		} catch (IllegalArgumentException iae) {
			CommandException ce = new CommandException(iae);
			ce.setLogStackDump(false);
			throw ce;
		}
		
		// Get sticky in channel
		boolean stickChannel = Boolean.valueOf(ctx.getParameter("stickyChannel")).booleanValue();
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Message Thread
			GetCoolerThreads dao = new GetCoolerThreads(con);
			MessageThread mt = dao.getThread(ctx.getID());
			if (mt == null)
				throw notFoundException("Invalid Thread - " + ctx.getID());

			// Get the DAO and the Channel
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel c = cdao.get(mt.getChannel());
			if (c == null)
				throw notFoundException("Invalid Channel - " + ctx.getID());

			// Check our access
			CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
			access.updateContext(mt, c);
			access.validate();
			if (!access.getCanRead() || !ctx.isUserInRole("Moderator"))
				throw securityException("Cannot update Message Thread sticky date");

			// Create the status update bean
			ThreadUpdate upd = new ThreadUpdate(mt.getID());
			upd.setAuthorID(ctx.getUser().getID());
			upd.setMessage("Message Thread stuck until " + StringUtils.format(stickyDate, "MMMM dd, yyyy HH:mm"));
			mt.setStickyUntil(stickyDate);

			// Start a transaction
			ctx.startTX();

			// Restick the thread, or if the date is in the past then unstick it
			SetCoolerMessage wdao = new SetCoolerMessage(con);
			wdao.restickThread(mt.getID(), stickyDate, stickChannel);
			wdao.write(upd);

			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward back to the thread
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("thread", null, ctx.getID());
		result.setSuccess(true);
	}
}