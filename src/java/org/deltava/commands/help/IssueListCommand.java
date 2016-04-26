// Copyright 2006, 2008, 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 7.0
 * @since 1.0
 */

public class IssueListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
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
			List<Issue> results = new ArrayList<Issue>(isActive ? idao.getActive() : idao.getAll());
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
			while (results.size() > vc.getCount())
				results.remove(results.size() - 1);
			
			// Get Author IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<Issue> i = results.iterator(); i.hasNext(); ) {
				Issue is = i.next();
				IDs.add(Integer.valueOf(is.getAuthorID()));
				IDs.add(Integer.valueOf(is.getAssignedTo()));
				IDs.add(Integer.valueOf(is.getLastCommentAuthorID()));
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