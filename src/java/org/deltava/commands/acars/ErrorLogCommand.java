// Copyright 2006, 2007, 2009, 2012, 2106, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.ACARSError;

import org.deltava.comparators.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to display ACARS client error reports.
 * @author Luke
 * @version 7.5
 * @since 1.0
 */

public class ErrorLogCommand extends AbstractViewCommand {
	
	private static final String[] FILTER_OPTS = {"All Reports", "By Author", "By Client Build"};
	private static final int ALL = 0;
	private static final int AUTHOR = 1;
	private static final int CLIENT = 2;

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view context and the search type
		ViewContext<ACARSError> vc = initView(ctx, ACARSError.class);
		int searchType = StringUtils.arrayIndexOf(FILTER_OPTS, ctx.getParameter("viewType"), ALL); 
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and view options
			GetACARSErrors dao = new GetACARSErrors(con);
			ctx.setAttribute("clientBuilds", dao.getBuilds(), REQUEST);
			Collection<Integer> authorIDs = dao.getPilots();
			
			// Get the error log
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			switch (searchType) {
				case AUTHOR :
					vc.setResults(dao.getByPilot(StringUtils.parse(ctx.getParameter("author"), -1)));
					break;
					
				case CLIENT :
					vc.setResults(dao.getByBuild(StringUtils.parse(ctx.getParameter("build"), -1)));
					break;
					
				case ALL:
				default:
					vc.setResults(dao.getAll());
			}
			
			// Get user IDs
			Collection<Integer> IDs = new HashSet<Integer>(authorIDs);
			vc.getResults().stream().map(AuthoredBean::getAuthorID).forEach(IDs::add);
			
			// Load the Users
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserDataMap udmap = uddao.get(IDs);
			Map<Integer, Pilot> pilots = pdao.get(udmap);
			
			// Get report author IDs
			Collection<Pilot> authors = new TreeSet<Pilot>(new PilotComparator(PersonComparator.FIRSTNAME));
			authorIDs.stream().map(id -> pilots.get(id)).filter(Objects::nonNull).forEach(authors::add);
			
			// Save the pilot profiles
			ctx.setAttribute("pilots", pilots, REQUEST);
			ctx.setAttribute("authors", authors, REQUEST);
			ctx.setAttribute("userdata", udmap, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save combo attributes
		ctx.setAttribute("filterOpts", ComboUtils.fromArray(FILTER_OPTS), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/acars/errorLog.jsp");
		result.setSuccess(true);
	}
}