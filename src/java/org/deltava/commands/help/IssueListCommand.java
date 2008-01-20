// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.help.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.HelpDeskAccessControl;

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
		boolean isActive = "active".equals(ctx.getCmdParameter(OPERATION, null));

		// Get the view start/end
		ViewContext vc = initView(ctx);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the issue list
			GetHelp idao = new GetHelp(con);
			idao.setQueryStart(vc.getStart());
			idao.setQueryMax(vc.getCount());
			Collection<Issue> results = isActive ? idao.getActive() : idao.getAll();
			vc.setResults(results);
			
			// Get Author IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<Issue> i = results.iterator(); i.hasNext(); ) {
				Issue is = i.next();
				IDs.add(new Integer(is.getAuthorID()));
				IDs.add(new Integer(is.getAssignedTo()));
				IDs.add(new Integer(is.getLastCommentAuthorID()));
			}
			
			// Load Pilot IDs
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set creation access control
		HelpDeskAccessControl ac = new HelpDeskAccessControl(ctx, null);
		ac.validate();
		ctx.setAttribute("access", ac, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/help/issueList.jsp");
		result.setSuccess(true);
	}
}