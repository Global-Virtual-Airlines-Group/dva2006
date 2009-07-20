// Copyright 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 2.6
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
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view context and the search type
		ViewContext vc = initView(ctx);
		int searchType = StringUtils.arrayIndexOf(FILTER_OPTS, ctx.getParameter("viewType"), ALL); 
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the log
			GetACARSErrors dao = new GetACARSErrors(con);
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
			
			// Load client builds
			ctx.setAttribute("clientBuilds", dao.getBuilds(), REQUEST);
			
			// Load Pilot IDs who have reported errors
			Collection<Integer> authorIDs = dao.getPilots();
			
			// Get user IDs
			Collection<Integer> IDs = new HashSet<Integer>(authorIDs);
			for (Iterator<?> i = vc.getResults().iterator(); i.hasNext(); ) {
				ACARSError err = (ACARSError) i.next();
				IDs.add(Integer.valueOf(err.getUserID()));
			}
			
			// Load the User IDs
			GetUserData uddao = new GetUserData(con);
			UserDataMap udmap = uddao.get(IDs);
			
			// Load the user profles
			GetPilot pdao = new GetPilot(con);
			Map<Integer, Pilot> pilots = pdao.get(udmap);
			
			// Get report author IDs
			Collection<Pilot> authors = new TreeSet<Pilot>(new PilotComparator(PersonComparator.FIRSTNAME));
			for (Iterator<Integer> i = authorIDs.iterator(); i.hasNext(); ) {
				Integer id = i.next();
				Pilot usr = pilots.get(id);
				if (usr != null)
					authors.add(usr);
			}
			
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