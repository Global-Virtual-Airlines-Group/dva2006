// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.academy.Course;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to display Flight Academy certifications.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CourseListCommand extends AbstractViewCommand {
	
	private static final List<ComboAlias> VIEW_OPTS = ComboUtils.fromArray(new String[] {"All Courses", 
			"Active Courses"}, new String[] {"all", "active"});

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Load the view context and if we are getting all courses
		ViewContext vc = initView(ctx);
		boolean isActive = "active".equals(ctx.getCmdParameter(OPERATION, "active"));
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the courses
			GetAcademyCourses dao = new GetAcademyCourses(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			Collection<Course> courses = isActive? dao.getActive() : dao.getAll();
			
			// Save in the request
			ctx.setAttribute("courses", courses, REQUEST);
			
			// Get the Pilot IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<Course> i = courses.iterator(); i.hasNext(); ) {
				Course c = i.next();
				IDs.add(new Integer(c.getPilotID()));
			}
			
			// Load the Pilot profiles
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save sort options
		ctx.setAttribute("sortOpt", isActive ? "active" : "all", REQUEST);
		ctx.setAttribute("viewOpts", VIEW_OPTS, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/courseList.jsp");
		result.setSuccess(true);
	}
}