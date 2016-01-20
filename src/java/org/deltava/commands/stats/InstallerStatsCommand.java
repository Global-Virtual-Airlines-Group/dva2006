// Copyright 2005, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;

import org.deltava.commands.*;

import org.deltava.dao.*;

import org.deltava.util.ComboUtils;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to retrieve Fleet Installer statistics.
 * @author Luke
 * @version 6.4
 * @since 1.0
 */

public class InstallerStatsCommand extends AbstractCommand {
	
	private static final String[] SORT_CODES = {"OS_VERSION", "NET_VERSION", "CPU", "CPU_CORE", "CPU_PROC", "GPU", "RAM", "VRAM", "LOCALE", "X", "Y"};
	private static final String[] SORT_LABELS = {"Operating System", ".NET Version", "CPU Type", "CPU Cores", "CPU Processors",
		"GPU Type", "Memory Size", "Video Memory", "Locale", "Screen Width", "Screen Height"};
	private static final List<?> SORT_OPTIONS = ComboUtils.fromArray(SORT_LABELS, SORT_CODES); 

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Save combobox choices
		String labelCode = ctx.getParameter("orderBy");
		ctx.setAttribute("sortOptions", SORT_OPTIONS, REQUEST);
		
		// If we're doing a GET, redirect to the JSP
		CommandResult result = ctx.getResult();
		if (labelCode == null) {
			result.setURL("/jsp/stats/systemStats.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Sanitize input
		int ofs = Math.max(0, StringUtils.arrayIndexOf(SORT_CODES, labelCode));
		ctx.setAttribute("labelCode", SORT_LABELS[ofs], REQUEST);

		// Check if we're sorting by label or results
		boolean sortLabel = Boolean.valueOf(ctx.getParameter("sortLabel")).booleanValue();
		try {
			GetSystemInfo dao = new GetSystemInfo(ctx.getConnection());
			ctx.setAttribute("total", Integer.valueOf(dao.getTotals()), REQUEST);
			ctx.setAttribute("stats", dao.getStatistics(SORT_CODES[ofs], sortLabel), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setURL("/jsp/stats/systemStats.jsp");
		result.setSuccess(true);
	}
}