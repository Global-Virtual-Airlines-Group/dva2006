// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.sql.Connection;

import org.deltava.beans.help.Issue;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.HelpDeskAccessControl;

/**
 * A Web Site Command to mark a Help Desk Issue as Closed.  
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IssueCloseCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Issue
			GetHelp idao = new GetHelp(con);
			Issue i = idao.getIssue(ctx.getID());
			if (i == null)
				throw notFoundException("Invalid Issue - " + ctx.getID());
			
			// Check our Access
			HelpDeskAccessControl ac = new HelpDeskAccessControl(ctx, i);
			ac.validate();
			if (!ac.getCanClose())
				throw securityException("Cannot Update Issue");
			
			// Update the status
			i.setStatus(Issue.CLOSED);
			
			// Save the issue
			SetHelp iwdao = new SetHelp(con);
			iwdao.write(i);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the Issue
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setURL("hdissue", null, ctx.getID());
		result.setSuccess(true);
	}
}