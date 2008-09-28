// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.Date;
import java.text.*;
import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.SetSystemLog;
import org.deltava.dao.DAOException;

/**
 * A Web Site Command to purge System Log entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class LogPurgeCommand extends AbstractCommand {

	private static final DateFormat _df = new SimpleDateFormat("MM/dd/yyyy");

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result
		CommandResult result = ctx.getResult();

		// If no log name specified, jump to the JSP
		String logName = ctx.getParameter("logName");
		if (logName == null) {
			result.setURL("/jsp/admin/logPurge.jsp");
			result.setSuccess(true);
			return;
		}

		// Get the purge date
		Date pd = null;
		try {
			pd = _df.parse(ctx.getParameter("purgeDate"));
		} catch (ParseException pe) {
			CommandException ce = new CommandException("Invalid Date - " + pe.getMessage());
			ce.setLogStackDump(false);
			throw ce;
		}

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and purge the log
			SetSystemLog wdao = new SetSystemLog(con);
			int entriesDeleted = wdao.purge(logName, pd);

			// Save the number of entries deleted
			ctx.setAttribute("rowsDeleted", new Integer(entriesDeleted), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/admin/logPurge.jsp");
		result.setSuccess(true);
	}
}
