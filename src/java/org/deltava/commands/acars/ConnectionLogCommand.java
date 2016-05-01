// Copyright 2005, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display ACARS Connection Log entries.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class ConnectionLogCommand extends ACARSLogViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// If we're not displaying anything, redirect to the result page
		CommandResult result = ctx.getResult();
		if (ctx.getParameter("pilotCode") == null) {
			result.setURL("/jsp/acars/connectionLog.jsp");
			result.setSuccess(true);
			return;
		}

		ViewContext<ConnectionEntry> vc = initView(ctx, ConnectionEntry.class);
		try {
			Connection con = ctx.getConnection();
			LogSearchCriteria criteria = getSearchCriteria(ctx, con);

			// Get the DAO and do the search
			GetACARSLog dao = new GetACARSLog(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getConnections(criteria));

			// Load the Pilot data
			GetUserData usrdao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserDataMap udm = usrdao.get(getPilotIDs(vc.getResults()));
			ctx.setAttribute("userData", udm, REQUEST);
			ctx.setAttribute("pilots", pdao.get(udm), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set search complete attribute
		ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/acars/connectionLog.jsp");
		result.setSuccess(true);
	}
}