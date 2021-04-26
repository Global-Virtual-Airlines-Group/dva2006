// Copyright 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ErrorLogAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to purge ACARS client error reports. 
 * @author Luke
 * @version 10.0
 * @since 9.1
 */

public class ErrorLogPurgeCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the purge type
		int searchType = StringUtils.arrayIndexOf(ErrorLogCommand.FILTER_OPTS, ctx.getParameter("viewType"));
		if (searchType < 1)
			throw notFoundException("Invalid purge criteria - " + ctx.getParameter("viewType"));

		// Get the build
		int build = StringUtils.parse(ctx.getParameter("build"), 0);
		int beta = StringUtils.parse(ctx.getParameter("beta"), -1);
		int userID = StringUtils.parse(ctx.getParameter("author"), -1);
		
		// Check our access
		ErrorLogAccessControl ac = new ErrorLogAccessControl(ctx, null);
		ac.validate();
		if (!ac.getCanDelete())
			throw securityException("Cannot purge Error Reports");
		
		try {
			SetACARSLog wdao = new SetACARSLog(ctx.getConnection());
			int recordsPurged = (searchType == 1) ? wdao.purgeByUser(userID) : wdao.purgeError(build, beta);
			ctx.setAttribute("purgeCount", Integer.valueOf(recordsPurged), REQUEST);
			ctx.setAttribute("errorPurge", Boolean.TRUE, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/acars/logEntryDelete.jsp");
		result.setSuccess(true);
	}
}