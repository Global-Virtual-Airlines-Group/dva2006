// Copyright 2005 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.sql.*;

import org.deltava.commands.*;

import org.deltava.dao.SetNavData;
import org.deltava.dao.DAOException;

/**
 * A Web Site Command to purge Navigation data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AIRACPurgeCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			ctx.startTX();
			
			// Get the DAO and purge the database
			SetNavData dao = new SetNavData(con);
			int rowsDeleted = dao.purge("NAVDATA");
			rowsDeleted += dao.purge("SID_STAR");
			rowsDeleted += dao.purge("AIRWAYS");
			
			// Save the rows deleted
			ctx.commitTX();
			ctx.setAttribute("rowsDeleted", new Integer(rowsDeleted), REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isPurge", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/schedule/navDataUpdate.jsp");
		result.setSuccess(true);
	}
}