// Copyright 2005, 2007, 2008, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;
import java.sql.Connection;
import java.time.ZonedDateTime;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to view the ACARS Text Message log.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public class MessageLogCommand extends ACARSLogViewCommand {

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
			result.setURL("/jsp/acars/msgLog.jsp");
			result.setSuccess(true);
			return;
		}

		ViewContext<String> vc = initView(ctx, String.class);
		try {
			Connection con = ctx.getConnection();
			LogSearchCriteria criteria = getSearchCriteria(ctx, con);
			
			// Save the date criteria
			if ((criteria.getStartDate() != null) && (criteria.getEndDate() != null)) {
				ctx.setAttribute("rangeStart", StringUtils.format(criteria.getStartDate(), "MM/dd/yyyy HH:mm"), REQUEST);
				ctx.setAttribute("rangeEnd", StringUtils.format(criteria.getEndDate(), "MM/dd/yyyy HH:mm"), REQUEST);
			}

			// Get the DAO and set start/count parameters
			GetACARSLog dao = new GetACARSLog(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());

			// Get the Messages
			Collection<TextMessage> results = dao.getMessages(criteria, ctx.getParameter("searchStr"));

			// Load the Pilot data
			GetUserData usrdao = new GetUserData(con);
			UserDataMap udm = usrdao.get(getPilotIDs(results));

			// Get the authors for each message
			GetPilot pdao = new GetPilot(con);
			Map<Integer, Pilot> pilots = pdao.get(udm);
			
			// Build the text representation of the messages
			Collection<String> txtResults = new ArrayList<String>();
			for (TextMessage msg : results) {
				ZonedDateTime dt = ZonedDateTime.ofInstant(msg.getDate(), ctx.getUser().getTZ().getZone());
				StringBuilder buf = new StringBuilder(StringUtils.format(dt, "MM/dd/yyyy HH:mm:ss"));
				buf.append(" <");
				Pilot msgFrom = pilots.get(Integer.valueOf(msg.getAuthorID()));
				buf.append((msgFrom == null) ? String.valueOf(msg.getAuthorID()) : msgFrom.getName());
				Pilot msgTo = pilots.get(Integer.valueOf(msg.getRecipientID()));
				if (msgTo != null) {
					buf.append('-');
					buf.append(msgTo.getName());
				}
				
				buf.append("> ");
				buf.append(msg.getMessage());
				txtResults.add(buf.toString());
			}

			// Save the results
			vc.setResults(txtResults);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set search complete attribute
		ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/acars/msgLog.jsp");
		result.setSuccess(true);
	}
}