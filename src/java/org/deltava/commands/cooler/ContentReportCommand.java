// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.cooler.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to report Water Cooler threads with questionable content.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ContentReportCommand extends AbstractCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

		boolean isLocked = false;
		MessageThread mt = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the message thread
			GetCoolerThreads dao = new GetCoolerThreads(con);
			mt = dao.getThread(ctx.getID());
			if (mt == null)
				throw notFoundException("Invalid Message Thread - " + ctx.getID());
			
			// Get the channel
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel c = cdao.get(mt.getChannel());
			if (c == null)
				throw notFoundException("Invalid Water Cooler channel - " + mt.getChannel());
			
			// Check our access
			CoolerThreadAccessControl ac = new CoolerThreadAccessControl(ctx);
			ac.updateContext(mt, c);
			ac.validate();
			if (!ac.getCanReport())
				throw securityException("Cannot report Message Thread");
			
			// Add a content warning entry
			ThreadUpdate upd = new ThreadUpdate(mt.getID());
			upd.setAuthorID(ctx.getUser().getID());
			upd.setMessage("Discussion Thread reported for potential inappropriate content");
			
			// Start a transaction
			ctx.startTX();
			
			// Write the update
			SetCoolerMessage wdao = new SetCoolerMessage(con);
			wdao.write(upd);
			
			// Write the warning
			wdao.report(mt, ctx.getUser().getID());
			
			// If we hit the limit for warnings, lock the thread
			mt.addReportID(ctx.getUser().getID());
			int maxWarns = SystemData.getInt("cooler.maxreports", 4);
			if (mt.getReportCount() == maxWarns) {
				wdao.moderateThread(mt.getID(), true, true);
				
				// Mark the thread as locked
				ThreadUpdate upd2 = new ThreadUpdate(mt.getID());
				upd2.setAuthorID(ctx.getUser().getID());
				upd2.setMessage("Discussion Thread automatically locked/hidden after " + maxWarns + " content reports");
				wdao.write(upd2);
				isLocked = true;
			}
			
			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setSuccess(true);
		if (isLocked && (!ctx.isUserInRole("Moderator")))
			result.setURL("channel.do", null, mt.getChannel());
		else 
			result.setURL("thread", null, mt.getID());
	}
}