// Copyright 2005, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.sql.Connection;

import org.deltava.beans.UserDataMap;
import org.deltava.beans.acars.FlightInfo;
import org.deltava.beans.acars.LogSearchCriteria;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to view the ACARS Flight Info log.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class FlightLogCommand extends ACARSLogViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// If we're not displaying anything, redirect to the result page
		CommandResult result = ctx.getResult();
		if (ctx.getParameter("pilotCode") == null) {
			result.setURL("/jsp/acars/flightLog.jsp");
			result.setSuccess(true);
			return;
		}

		ViewContext<FlightInfo> vc = initView(ctx, FlightInfo.class);
		try {
			Connection con = ctx.getConnection();
			LogSearchCriteria criteria = getSearchCriteria(ctx, con);

			// Get the DAO and set start/count parameters
			GetACARSLog dao = new GetACARSLog(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getFlights(criteria));

			// Load the Pilot data
			GetUserData usrdao = new GetUserData(con);
			UserDataMap udm = usrdao.get(getPilotIDs(vc.getResults()));
			ctx.setAttribute("userData", udm, REQUEST);

			// Get the authors for each log entry
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.get(udm), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set search complete attribute
		ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/acars/flightLog.jsp");
		result.setSuccess(true);
	}
}