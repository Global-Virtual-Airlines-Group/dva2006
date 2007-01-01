// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.comparators.PilotComparator;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to search for duplicate Pilots.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class DuplicatePilotSearchCommand extends AbstractCommand {

	private static final int DEFAULT_RESULTS = 5;

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command results
		CommandResult result = ctx.getResult();
		if (ctx.getParameter("firstName") == null) {
			ctx.setAttribute("noResults", Boolean.TRUE, REQUEST);
			ctx.setAttribute("maxResults", new Integer(DEFAULT_RESULTS), REQUEST);
			result.setURL("/jsp/roster/dupeSearch.jsp");
			result.setSuccess(true);
			return;
		}

		// Check if we're doing an exact match
		boolean exactMatch = Boolean.valueOf(ctx.getParameter("exactMatch")).booleanValue();

		// Build the parameters
		String fName1 = buildParameter(ctx.getParameter("firstName"), exactMatch);
		String lName1 = buildParameter(ctx.getParameter("lastName"), exactMatch);
		String fName2 = buildParameter(ctx.getParameter("firstName2"), exactMatch);
		String lName2 = buildParameter(ctx.getParameter("lastName2"), exactMatch);

		// Set result set size
		int maxResults = StringUtils.parse(ctx.getParameter("maxResults"), DEFAULT_RESULTS);
		if ((maxResults < 1) || (maxResults > 99))
			maxResults = DEFAULT_RESULTS;

		Collection<Pilot> results = new TreeSet<Pilot>(new PilotComparator(PilotComparator.PILOTCODE));
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and search
			GetPilot dao = new GetPilot(con);
			dao.setQueryMax(maxResults);
			results.addAll(dao.search(fName1, lName1, null));
			results.addAll(dao.search(fName2, lName2, null));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Build the possible IDs
		Collection<String> pilotIDs = new LinkedHashSet<String>();
		for (Iterator<Pilot> i = results.iterator(); i.hasNext();) {
			Pilot usr = i.next();
			if (!StringUtils.isEmpty(usr.getPilotCode()))
				pilotIDs.add(usr.getPilotCode());
		}

		// Save the results in the request
		ctx.setAttribute("results", results, REQUEST);
		ctx.setAttribute("pilotCodes", pilotIDs, REQUEST);
		ctx.setAttribute("maxResults", new Integer(maxResults), REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/roster/dupeSearch.jsp");
		result.setSuccess(true);
	}

	/**
	 * Helper method to take a parameter and add LIKE wildcards.
	 */
	private String buildParameter(String pValue, boolean exMatch) {
		if (StringUtils.isEmpty(pValue))
			return null;
		return exMatch ? pValue : "%" + pValue + "%";
	}
}