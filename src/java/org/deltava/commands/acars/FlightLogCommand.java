// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.UserDataMap;
import org.deltava.beans.acars.LogSearchCriteria;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to view the ACARS Flight Info log.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightLogCommand extends ACARSLogViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view context and the search type
		ViewContext vc = initView(ctx);

		// Get the command result
		CommandResult result = ctx.getResult();

		// If we're not displaying anything, redirect to the result page
		if (ctx.getParameter("pilotCode") == null) {
			result.setURL("/jsp/acars/flightLog.jsp");
			result.setSuccess(true);
			return;
		}

		try {
			Connection con = ctx.getConnection();
			LogSearchCriteria criteria = getSearchCriteria(ctx, con);

			// Get the DAO and set start/count parameters
			GetACARSLog dao = new GetACARSLog(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());

			// Do the search
			vc.setResults(dao.getFlights(criteria));

			// Load the Pilot data
			GetUserData usrdao = new GetUserData(con);
			UserDataMap udm = usrdao.get(getPilotIDs(vc.getResults()));
			ctx.setAttribute("userData", udm, REQUEST);

			// Get the authors for each log entry
			Map<Integer, Pilot> pilots = new HashMap<Integer, Pilot>();
			GetPilot pdao = new GetPilot(con);
			for (Iterator<String> i = udm.getTableNames().iterator(); i.hasNext();) {
				String dbTableName = i.next();
				pilots.putAll(pdao.getByID(udm.getByTable(dbTableName), dbTableName));
			}

			// Save the pilots in the request
			ctx.setAttribute("pilots", pilots, REQUEST);
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