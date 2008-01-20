// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.help.Issue;

import org.deltava.comparators.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to view Help Desk issues by Author or Assignee.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class IssueFilterCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Determine if we want by author or assignee
		boolean isAuthor = "author".equals(ctx.getCmdParameter(OPERATION, null));
		int id = (ctx.getID() == 0) ? ctx.getUser().getID() : ctx.getID();
		ctx.setAttribute("isAuthor", Boolean.valueOf(isAuthor), REQUEST);

		// Get the view start/end
		ViewContext vc = initView(ctx);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the available authors/assignees
			GetHelp idao = new GetHelp(con);
			Collection<Integer> choiceIDs = isAuthor ? idao.getAuthors() : idao.getAssignees();
					
			// Get the issues
			idao.setQueryStart(vc.getStart());
			idao.setQueryMax(vc.getCount());
			Collection<Issue> results = idao.getByPilot(isAuthor ? id : -1, isAuthor ? -1 : id, false);
			vc.setResults(results);
			
			// Get Author IDs
			Collection<Integer> IDs = new HashSet<Integer>(choiceIDs);
			for (Iterator<Issue> i = results.iterator(); i.hasNext(); ) {
				Issue is = i.next();
				IDs.add(new Integer(is.getAuthorID()));
				IDs.add(new Integer(is.getAssignedTo()));
				IDs.add(new Integer(is.getLastCommentAuthorID()));
			}
			
			// Load Pilot IDs and populate drop down
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
			List<Pilot> authors = new ArrayList<Pilot>(pdao.getByID(choiceIDs, "PILOTS").values());
			Collections.sort(authors, new PilotComparator(PersonComparator.FIRSTNAME));
			ctx.setAttribute("authors", authors, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/help/issueFilter.jsp");
		result.setSuccess(true);
	}
}