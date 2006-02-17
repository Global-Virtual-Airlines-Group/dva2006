// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.academy.Course;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.CourseAccessControl;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to change a Flight Academy Course's status.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CourseDisposalCommand extends AbstractCommand {
	
	// Operation constants
	private static final String[] OPNAMES = {"restart", "abandon", "complete"};

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the operation
		String opName = (String) ctx.getCmdParameter(Command.OPERATION, null);
		int opCode = StringUtils.arrayIndexOf(OPNAMES, opName);
		if (opCode < 1)
			throw new CommandException("Invalid Operation - " + opName);

		// Initialize the Message Context
		MessageContext mctx = new MessageContext();
		mctx.addData("user", ctx.getUser());

		Pilot usr = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Course
			GetAcademyCourses dao = new GetAcademyCourses(con);
			Course c = dao.get(ctx.getID());
			if (c == null)
				throw notFoundException("Invalid Course - " + ctx.getID());
			
			// Get the Message Template DAO
			GetMessageTemplate mtdao = new GetMessageTemplate(con);

			// Check our access
			CourseAccessControl access = new CourseAccessControl(ctx, c);
			access.validate();
			boolean canExec = false;
			switch (opCode) {
				case Course.STARTED :
					ctx.setAttribute("isRestarted", Boolean.TRUE, REQUEST);
					canExec = access.getCanRestart() || access.getCanStart();
					break;
					
				case Course.ABANDONED :
					canExec = access.getCanCancel();
					mctx.setTemplate(mtdao.get("COURSECANCEL"));
					ctx.setAttribute("isAbandoned", Boolean.TRUE, REQUEST);
					break;
					
				case Course.COMPLETE :
					canExec = access.getCanApprove();
					mctx.setTemplate(mtdao.get("COURSECOMPLETE"));
					ctx.setAttribute("isCompleted", Boolean.TRUE, REQUEST);
					break;
			}
			
			// If we can't execute the command, stop
			if (!canExec)
				throw securityException("Cannot " + opName + " - Not Authorized");

			// Get the Pilot
			if (ctx.getUser().getID() != c.getPilotID()) {
				GetPilot pdao = new GetPilot(con);
				usr = pdao.get(c.getPilotID());
			}
			
			// Get the DAO and update the course 
			SetAcademy wdao = new SetAcademy(con);
			wdao.setStatus(c.getID(), opCode);
			
			// Save the course
			ctx.setAttribute("course", c, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Send a notification message
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctx);
		mailer.send(usr);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/courseUpdate.jsp");
		result.setType(CommandResult.REQREDIRECT);
		result.setSuccess(true);
	}
}