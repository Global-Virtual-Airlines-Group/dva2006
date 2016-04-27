// Copyright 2006, 2008, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.help.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.HelpDeskAccessControl;

/**
 * A Web Site Command to display a Pilot's Help Desk issues.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class MyIssueListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext vc = initView(ctx);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the issue list
			int myID = ctx.getUser().getID();
			GetHelp idao = new GetHelp(con);
			idao.setQueryStart(vc.getStart());
			idao.setQueryMax(vc.getCount());
			Collection<Issue> results = idao.getByPilot(myID, myID, false, false);
			vc.setResults(results);
			
			// Get Author IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Issue is : results) {
				IDs.add(Integer.valueOf(is.getAuthorID()));
				IDs.add(Integer.valueOf(is.getAssignedTo()));
				IDs.add(Integer.valueOf(is.getLastCommentAuthorID()));
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
		ctx.setAttribute("cmdName", getID(), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/help/issueList.jsp");
		result.setSuccess(true);
	}
}