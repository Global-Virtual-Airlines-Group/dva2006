// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetInstallerSystemInfo;
import org.deltava.dao.DAOException;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to retrieve Fleet Installer statistics.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class InstallerStatsCommand extends AbstractCommand {
	
	private static final List<?> _sortOptions = ComboUtils.fromArray(new String[] {"Installer Code", "Operating System", "Memory Size", 
		"Flight Simulator Version"}, new String[] {"INSTALLER", "OS", "MEMORY", "FSVERSION"});

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Save combobox choices
		ctx.setAttribute("sortOptions", _sortOptions, REQUEST);
		
		// Get the Command Result
		CommandResult result = ctx.getResult();

		// If we're doing a GET, redirect to the JSP
		if (ctx.getParameter("orderBy") == null) {
			result.setURL("/jsp/fleet/logStats.jsp");
			result.setSuccess(true);
			return;
		}

		// Check if we're sorting by label or results
		boolean sortLabel = "1".equals(ctx.getParameter("sortLabel"));

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the statistics
			GetInstallerSystemInfo dao = new GetInstallerSystemInfo(con);
			ctx.setAttribute("stats", dao.getStatistics(ctx.getParameter("orderBy"), sortLabel), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setURL("/jsp/fleet/logStats.jsp");
		result.setSuccess(true);
	}
}