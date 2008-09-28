// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
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
			
			// Start a transaction
			ctx.startTX();
			
			// Loop through the progress entries
			SetAcademy wdao = new SetAcademy(con);
			for (Iterator<CourseProgress> i = c.getProgress().iterator(); i.hasNext(); ) {
				CourseProgress cp = i.next();
			
				// Get the status
				boolean isComplete = Boolean.valueOf(ctx.getParameter("progress" + cp.getID())).booleanValue();
				if (isComplete != cp.getComplete()) {
					cp.setAuthorID(ctx.getUser().getID());
					cp.setComplete(isComplete);
					cp.setCompletedOn(isComplete ? new Date() : null);
					wdao.updateProgress(cp);
				}
			}
			
			// Commit transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the course
		CommandResult result = ctx.getResult();
		result.setURL("course", null, ctx.getID());
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}