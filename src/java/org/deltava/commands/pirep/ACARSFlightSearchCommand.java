// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.text.*;
import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;

/**
 * A Web Site Command to search for ACARS Flight Reports.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class ACARSFlightSearchCommand extends AbstractViewCommand {

	private static final int EVENT = 0;
	private static final int DATE = 1;
	private static final int PILOT = 2;
	private static final String[] SEARCH_TYPES = { "Online Event", "Date", "My Flights" };

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command results
		CommandResult result = ctx.getResult();
		
		// Calculate the search types
		List<?> sTypes = ComboUtils.fromArray(SEARCH_TYPES);
		if (!ctx.isAuthenticated())
			sTypes.remove(sTypes.size() - 1);
		
		// Save the search types
		ctx.setAttribute("searchTypes", sTypes, REQUEST);

		// Get the view context and search type
		ViewContext vc = initView(ctx);
		int searchType = StringUtils.arrayIndexOf(SEARCH_TYPES, ctx.getParameter("searchType"));
		if (searchType == -1)
			searchType = ctx.isAuthenticated() ? PILOT : DATE;
		else if ((searchType == PILOT) && !ctx.isAuthenticated())
			searchType = DATE;

		try {
			Connection con = ctx.getConnection();

			// Get available Events
			GetEvent evdao = new GetEvent(con);
			ctx.setAttribute("events", evdao.getWithACARS(), REQUEST);
			
			// Get the search type
			if (ctx.getParameter("searchType") == null) {
				ctx.release();
				result.setURL("/jsp/acars/acarsSearch.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Get the DAO
			GetFlightReportACARS dao = new GetFlightReportACARS(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());

			// Do the search
			switch (searchType) {
				case EVENT:
					String id = ctx.getParameter("eventID");
					try {
						int eventID = StringUtils.parseHex(id);
						if (evdao.get(eventID) == null)
							throw new IllegalArgumentException(id);
							
						vc.setResults(dao.getByEvent(eventID));	
					} catch (NumberFormatException nfe) {
						ctx.setMessage("Invalid Event - " + id);
					} catch (IllegalArgumentException iae) {
						ctx.setMessage("Unknown Event - " + iae.getMessage());
					}
					
					break;

				case DATE:
					try {
						DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
						vc.setResults(dao.getByDate(df.parse(ctx.getParameter("flightDate"))));	
					} catch (Exception e) {
						vc.setResults(dao.getByDate(new Date()));
					}
					
					break;

				default:
					vc.setResults(dao.getByPilot(ctx.getUser().getID(), "DATE DESC"));
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set search attribute
		ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		result.setURL("/jsp/acars/acarsSearch.jsp");
		result.setSuccess(true);
	}
}