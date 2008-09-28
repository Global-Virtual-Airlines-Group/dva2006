// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.sql.Connection;

import org.deltava.beans.academy.Course;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CourseAccessControl;

/**
 * A Web Site Command to delete Flight Academy courses.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CourseDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the course
			GetAcademyCourses dao = new GetAcademyCourses(con);
			Course c = dao.get(ctx.getID());
			if (c == null)
				throw notFoundException("Invalid Course - " + ctx.getID());
			
			// Check our access
			CourseAccessControl access = new CourseAccessControl(ctx, c);
			access.validate();
			if (!access.getCanDelete())
				throw securityException("Cannot delete Course");
			
			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilot", pdao.get(c.getPilotID()), REQUEST);
			
			// Save in the request
			ctx.setAttribute("course", c, REQUEST);
			
			// Get the write DAO and delete the Course
			SetAcademy wdao = new SetAcademy(con);
			wdao.delete(c.getID());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/courseUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}