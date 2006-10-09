// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.academy.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AcademyIssueAccessControl;

/**
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MyIssueListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view start/end
		ViewContext vc = initView(ctx);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the issue list
			GetAcademyIssues idao = new GetAcademyIssues(con);
			idao.setQueryStart(vc.getStart());
			idao.setQueryMax(vc.getCount());
			Collection<Issue> results = idao.getByPilot(ctx.getUser().getID());
			vc.setResults(results);
			
			// Get Author IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<Issue> i = results.iterator(); i.hasNext(); ) {
				Issue is = i.next();
				IDs.add(new Integer(is.getAuthorID()));
				IDs.add(new Integer(is.getAssignedTo()));
				IDs.add(new Integer(is.getLastCommentAuthorID()));
			}

			// Get the course list
			GetAcademyCourses cdao = new GetAcademyCourses(con);
			cdao.setQueryMax(1);
			Collection<Course> myCourses = cdao.getByPilot(ctx.getUser().getID());
			
			// Set creation access control
			AcademyIssueAccessControl ac = new AcademyIssueAccessControl(ctx, null, !myCourses.isEmpty());
			ac.validate();
			ctx.setAttribute("access", ac, REQUEST);

			// Load Pilot IDs
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/issueList.jsp");
		result.setSuccess(true);
	}
}