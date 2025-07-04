// Copyright 2008, 2016 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 7.0
 * @since 2.1
 */

public class IssueFilterCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Determine if we want by author or assignee
		boolean isAuthor = "author".equals(ctx.getCmdParameter(OPERATION, null));
		int id = (ctx.getID() == 0) ? ctx.getUser().getID() : ctx.getID();
		ctx.setAttribute("isAuthor", Boolean.valueOf(isAuthor), REQUEST);
		boolean showActive = Boolean.valueOf(ctx.getParameter("activeOnly")).booleanValue();

		// Get the view start/end
		ViewContext<Issue> vc = initView(ctx, Issue.class);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the available authors/assignees
			GetHelp idao = new GetHelp(con);
			Collection<Integer> choiceIDs = isAuthor ? idao.getAuthors() : idao.getAssignees();
					
			// Get the issues
			idao.setQueryStart(vc.getStart());
			idao.setQueryMax(vc.getCount());
			vc.setResults(idao.getByPilot(isAuthor ? id : -1, isAuthor ? -1 : id, false, showActive));
			
			// Get Author IDs
			Collection<Integer> IDs = new HashSet<Integer>(choiceIDs);
			for (Issue is : vc.getResults()) {
				IDs.add(Integer.valueOf(is.getAuthorID()));
				IDs.add(Integer.valueOf(is.getAssignedTo()));
				IDs.add(Integer.valueOf(is.getLastCommentAuthorID()));
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