// Copyright 2005, 2006, 2007, 2010, 2012, 2015, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.comparators.PilotComparator;

import org.deltava.util.*;

/**
 * A Web Site Command to search for duplicate Pilots.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class DuplicatePilotSearchCommand extends AbstractCommand {

	private static final int DEFAULT_RESULTS = 10;

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command results
		CommandResult result = ctx.getResult();
		if (ctx.getParameter("firstName") == null) {
			ctx.setAttribute("noResults", Boolean.TRUE, REQUEST);
			ctx.setAttribute("maxResults", Integer.valueOf(DEFAULT_RESULTS), REQUEST);
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
			GetPilot dao = new GetPilot(ctx.getConnection());
			dao.setQueryMax(maxResults);
			results.addAll(dao.search(ctx.getDB(), fName1, lName1));
			results.addAll(dao.search(ctx.getDB(), fName2, lName2));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Build the possible IDs
		Collection<ComboAlias> pilotInfo = new LinkedHashSet<ComboAlias>(); 
		Collection<String> pilotIDs = new LinkedHashSet<String>();
		for (Pilot usr : results) {
			StringBuilder buf = new StringBuilder(usr.getName());
			if (!StringUtils.isEmpty(usr.getPilotCode())) {
				pilotIDs.add(usr.getPilotCode());
				buf.append(" [ ");
				buf.append(usr.getPilotCode());
				buf.append(" ] ");
			}

			// Add the name option
			String joinDate = StringUtils.format(usr.getCreatedOn(), ctx.getUser().getDateFormat());
			pilotInfo.add(ComboUtils.fromString(buf.toString() +" (joined on " +  joinDate + ")", usr.getHexID()));
		}

		// Save the results in the request
		ctx.setAttribute("results", results, REQUEST);
		ctx.setAttribute("pilotCodes", pilotIDs, REQUEST);
		ctx.setAttribute("pilotChoices", pilotInfo, REQUEST);
		ctx.setAttribute("maxResults", Integer.valueOf(maxResults), REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/roster/dupeSearch.jsp");
		result.setSuccess(true);
	}

	/*
	 * Helper method to take a parameter and add LIKE wildcards.
	 */
	private static String buildParameter(String pValue, boolean exMatch) {
		if (StringUtils.isEmpty(pValue)) return null;
		return exMatch ? pValue : "%" + pValue + "%";
	}
}