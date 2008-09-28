// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.sql.Connection;

import org.deltava.beans.fleet.Manual;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.FleetEntryAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to delete a Manual.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ManualDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the file to delete
		String fName = (String) ctx.getCmdParameter(ID, null);

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the library entry
			GetDocuments dao = new GetDocuments(con);
			Manual m = dao.getManual(fName, SystemData.get("airline.db"));
			if (m == null)
				throw notFoundException("Invalid file name - " + fName);

			// Check our access
			FleetEntryAccessControl access = new FleetEntryAccessControl(ctx, m);
			access.validate();
			if (!access.getCanDelete())
				throw securityException("Cannot delete File Library entry");
			
	         // Delete the entry
	         SetLibrary wdao = new SetLibrary(con);
	         wdao.delete(m);
			
	         // Save the entry
	         ctx.setAttribute("manual", m, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save status attributes
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
		ctx.setAttribute("library", "Document", REQUEST);
		ctx.setAttribute("librarycmd", "doclibrary", REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/fleet/libraryUpdate.jsp");
		result.setSuccess(true);
	}
}