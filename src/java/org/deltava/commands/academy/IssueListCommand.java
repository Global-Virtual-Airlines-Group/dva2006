// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.academy.Issue;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AcademyIssueControl;

/**
 * A Web Site Command to display Flight Academy Issues.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IssueListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Determine if we want active or all issues
		boolean isActive = Boolean.valueOf(ctx.getParameter("isActive")).booleanValue();

		// Get the view start/end
		ViewContext vc = initView(ctx);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the issue list
			GetAcademyIssues idao = new GetAcademyIssues(con);
			idao.setQueryStart(vc.getStart());
			idao.setQueryMax(vc.getCount());
			Collection<Issue> results = isActive ? idao.getActive() : idao.getAll();
			vc.setResults(results);
			
			// Get the course list
			
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set creation access control
		AcademyIssueControl ac = new AcademyIssueControl(ctx, null, false);
		ac.validate();
		ctx.setAttribute("access", ac, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/issueList.jsp");
		result.setSuccess(true);
	}
}