// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.main;

import java.sql.Connection;

import org.deltava.beans.StatusUpdate;
import org.deltava.commands.*;

import org.deltava.dao.GetStatusUpdate;
import org.deltava.dao.DAOException;

/**
 * A Web Site Command to display Pilot accomplishments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PilotRecognitionCommand extends AbstractCommand {

	 /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO
			GetStatusUpdate dao = new GetStatusUpdate(con);
			dao.setQueryMax(10);
			
			// Get promotions and rank changes
			ctx.setAttribute("promotions", dao.getByType(StatusUpdate.EXTPROMOTION), REQUEST);
			ctx.setAttribute("rankChanges", dao.getByType(StatusUpdate.INTPROMOTION), REQUEST);
			ctx.setAttribute("ratingChanges", dao.getByType(StatusUpdate.RATING_ADD), REQUEST);
			
			// Get pilot recognition
			ctx.setAttribute("recognition", dao.getByType(StatusUpdate.RECOGNITION), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/roster/pilotAccomplishments.jsp");
		result.setSuccess(true);
	}
}