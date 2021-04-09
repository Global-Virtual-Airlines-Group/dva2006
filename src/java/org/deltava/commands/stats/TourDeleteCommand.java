// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.sql.Connection;

import org.deltava.beans.stats.Tour;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.TourAccessControl;

/**
 * A Web Site Command to delete Tour profiles. 
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class TourDeleteCommand extends AbstractCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs.
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the tour
			GetTour tdao = new GetTour(con);
			Tour t = tdao.get(ctx.getID(), ctx.getDB());
			if (t == null)
				throw notFoundException("Invalid Tour ID - " + ctx.getID());
			
			// Check our access
			TourAccessControl ac = new TourAccessControl(ctx, t);
			ac.validate();
			if (!ac.getCanDelete())
				throw securityException("Cannot delete Tour profile - " + t.getID());
			
			// Delete the tour
			SetTour twdao = new SetTour(con);
			int legsUpdated = twdao.delete(t);
			
			// Set status attributes
			ctx.setAttribute("tour", t, REQUEST);
			ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
			ctx.setAttribute("legsUpdated", Integer.valueOf(legsUpdated), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/tourUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}