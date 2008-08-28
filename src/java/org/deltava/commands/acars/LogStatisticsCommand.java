// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display ACARS server Command statistics. 
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class LogStatisticsCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Save the ACARS statistics in the request
		try {
			Connection con = ctx.getConnection();
			GetACARSLog addao = new GetACARSLog(con);
			ctx.setAttribute("acarsCmdStats", addao.getCommandStats(), REQUEST);
			GetACARSBandwidth bwdao = new GetACARSBandwidth(con);
			ctx.setAttribute("acarsBW", bwdao.getLatest(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/acars/cmdLogStats.jsp");
		result.setSuccess(true);
	}
}