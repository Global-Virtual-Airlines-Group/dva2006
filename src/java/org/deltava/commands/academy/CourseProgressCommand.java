// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.Date;
import java.sql.Connection;

import org.deltava.beans.academy.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.CourseAccessControl;

/**
 * A Web Site Command to track Flight Academy course progress.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CourseProgressCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Initialize the Message Context
		MessageContext mctx = new MessageContext();
		mctx.addData("user", ctx.getUser());

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Course
			GetAcademyCourses dao = new GetAcademyCourses(con);
			Course c = dao.get(ctx.getID());
			if (c == null)
				throw notFoundException("Invalid Course - " + ctx.getID());
			
			// Check our access
			CourseAccessControl access = new CourseAccessControl(ctx, c);
			access.validate();
			if (!access.getCanUpdateProgress())
				throw securityException("Cannot update progress");
			
			// Loop through the fields on the form
			int seq = 0;

			// Get the course progress entry
			CourseProgress cp = c.getProgressEntry(seq);
			if (cp == null)
				throw notFoundException("Cannot find entry " + seq);
			
			// Update completion
			boolean isComplete = Boolean.valueOf((String) ctx.getCmdParameter(OPERATION, "false")).booleanValue();
			if ((isComplete) && (!cp.getComplete())) {
				cp.setComplete(true);
				cp.setCompletedOn(new Date());
			} else if (!isComplete) {
				cp.setComplete(false);
				cp.setCompletedOn(null);
			}
			
			// Get the write DAO and update the progress entry
			SetAcademy wdao = new SetAcademy(con);
			wdao.updateProgress(cp);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the course
		CommandResult result = ctx.getResult();
		result.setURL("course", null, ctx.getID());
		result.setType(CommandResult.REDIRECT);
		result.setSuccess(true);
	}
}