// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.sql.Connection;

import org.deltava.beans.academy.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AcademyIssueAccessControl;

/**
 * A Web Site Command to save Flight Academy Issue comments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IssueCommentCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();
			
			// Get the Issue
			GetAcademyIssues dao = new GetAcademyIssues(con);
			Issue i = dao.getIssue(ctx.getID());
			if (i == null)
				throw notFoundException("Invalid Issue - " + ctx.getID());
			
			// Check our Access
			AcademyIssueAccessControl ac = new AcademyIssueAccessControl(ctx, i, false);
			ac.validate();
			if (!ac.getCanComment())
				throw securityException("Cannot comment on Issue " + i.getID());
			
			// Build the Issue Comment
			IssueComment ic = new IssueComment(ctx.getUser().getID());
			ic.setID(i.getID());
			ic.setBody(ctx.getParameter("body"));
			
			// Save the comment
			SetAcademyIssue iwdao = new SetAcademyIssue(con);
			iwdao.write(ic);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Redirect back to the Issue
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setURL("academyissue", null, ctx.getID());
		result.setSuccess(true);
	}
}