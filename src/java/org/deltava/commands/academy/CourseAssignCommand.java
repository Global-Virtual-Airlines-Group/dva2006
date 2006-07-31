// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.academy.Course;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CourseAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to assign an Instructor to a Flight Academy Course.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CourseAssignCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		Course c = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Course
			GetAcademyCourses dao = new GetAcademyCourses(con);
			c = dao.get(ctx.getID());
			if (c == null)
				throw notFoundException("Invalid Course - " + ctx.getID());
			
			// Check our access
			CourseAccessControl ac = new CourseAccessControl(ctx, c);
			ac.validate();
			if (!ac.getCanAssign())
				throw securityException("Cannot assign Instructor");
			
			// Parse the instructor ID
			String insID = ctx.getParameter("instructor");
			int id = insID.startsWith("0x") ? StringUtils.parseHex(insID) : 0;
			
			// Get the instructor
			if (id != 0) {
				GetPilot pdao = new GetPilot(con);
				Pilot ins = pdao.get(id);
				if (ins == null)
					throw notFoundException("Invalid Pilot ID - " + insID);
				else if (!ins.isInRole("Instructor"))
					throw securityException(ins.getName() + " not an Instructor");
			}
			
			// Update the course
			c.setInstructorID(id);
			
			// Save the course
			SetAcademy wdao = new SetAcademy(con);
			wdao.write(c);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward back to the Course
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setURL("course.do", null, c.getID());
		result.setSuccess(true);
	}
}