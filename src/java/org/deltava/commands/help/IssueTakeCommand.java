// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.sql.Connection;

import org.deltava.beans.help.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.HelpDeskAccessControl;

/**
 * A Web Site Command to reassign a Help Desk Issue to the current User.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class IssueTakeCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Issue
			GetHelp idao = new GetHelp(con);
			Issue i = idao.getIssue(ctx.getID());
			if (i == null)
				throw notFoundException("Invalid Issue", ctx.getID());
			
			// Check our Access
			HelpDeskAccessControl ac = new HelpDeskAccessControl(ctx, i);
			ac.validate();
			if (!ac.getCanTake())
				throw securityException("Cannot take Issue");
			
			// Log reassignment in comments
			i.setAssignedTo(ctx.getUser().getID());
			i.setStatus(IssueStatus.ASSIGNED);
			IssueComment ic = new IssueComment(ctx.getUser().getID());
			ic.setBody("Reassigned Help Desk Issue to myself");
			ic.setID(i.getID());
			i.addComment(ic);

			// Save the issue
			ctx.startTX();
			SetHelp iwdao = new SetHelp(con);
			iwdao.write(i);
			iwdao.write(ic);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the Issue
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("hdissue", null, ctx.getID());
		result.setSuccess(true);
	}
}