// Copyright 2005, 2006, 2009, 2012, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.cooler.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

/**
 * A Web Site Command to delete Water Cooler message threads.
 * @author Luke
 * @version 10.2
 * @since 1.0
 */

public class ThreadDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		CommandResult result = ctx.getResult();
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Thread
			GetCoolerThreads dao = new GetCoolerThreads(con);
			MessageThread mt = dao.getThread(ctx.getID());
			if (mt == null)
				throw notFoundException("Invalid Message Thread - " + ctx.getID());

			// Get the Cooler Channel
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel c = cdao.get(mt.getChannel());
			if (c == null)
				throw notFoundException("Invalid Channel - " + mt.getChannel());

			// Validate our access
			CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
			access.updateContext(mt, c);
			access.validate();
			if (!access.getCanDelete())
				throw securityException("Cannot delete Thread");
			
			// Save the thread in the request
			ctx.setAttribute("thread", mt, REQUEST);
			
			// If we're not forcing a delete, redirect
			boolean isOK = "force".equals(ctx.getCmdParameter(OPERATION, null));
			if (!isOK) {
				ctx.release();
				
				result.setURL("/jsp/cooler/threadDeleteConfirm.jsp");
				result.setSuccess(true);
				return;
			}

			// Start transaction
			ctx.startTX();

			// If there's an image, nuke the image
			if (mt.getImage() != 0) {
				SetGalleryImage iwdao = new SetGalleryImage(con);
				iwdao.delete(mt.getImage());
			}

			// Delete thread notifications
			SetCoolerNotification nwdao = new SetCoolerNotification(con);
			nwdao.clear(mt.getID());

			// Get the write DAO and delete the thread
			SetCoolerMessage twdao = new SetCoolerMessage(con);
			twdao.delete(mt.getID());
			
			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status attribute
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/cooler/threadUpdate.jsp");
		result.setSuccess(true);
	}
}