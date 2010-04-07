// Copyright 2006, 2008, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.help.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.*;

/**
 * A Web Site Command to display Help Desk Issues.
 * @author Luke
 * @version 3.0
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
			idao.setQueryMax(Math.round(vc.getCount() * 1.275f));
			
			// Load issues
			Collection<Issue> results = isActive ? idao.getActive() : idao.getAll();
			for (Iterator<Issue> ii = results.iterator(); ii.hasNext(); ) {
				Issue i = ii.next();
				HelpDeskAccessControl ac = new HelpDeskAccessControl(ctx, i);
				try {
					ac.validate();
				} catch (AccessControlException ace) {
					ii.remove();
				}
			}
			
			// Trim issues
			if (results.size() > vc.getCount()) {
				List<Issue> iList = new ArrayList<Issue>(results);
				iList.removeAll(iList.subList(vc.getCount(), iList.size()));
			}
			
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
			
			// Save issues
			vc.setResults(results);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set creation access control
		HelpDeskAccessControl ac = new HelpDeskAccessControl(ctx, null);
		ac.validate();
		ctx.setAttribute("access", ac, REQUEST);
		ctx.setAttribute("cmdName", getID(), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/help/issueList.jsp");
		result.setSuccess(true);
	}
}