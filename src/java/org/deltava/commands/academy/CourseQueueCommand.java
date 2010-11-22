// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.academy.Course;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display Flight Academy courses ready for approval.
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public class CourseQueueCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the view start/end
		ViewContext vc = initView(ctx);
		try {
			Connection con = ctx.getConnection();
			
			// Load the pending check rides
			GetAcademyCourses cdao = new GetAcademyCourses(con);
			cdao.setQueryStart(vc.getStart());
			cdao.setQueryMax(vc.getCount());
			Collection<Course> queue = cdao.getCompletionQueue();
			vc.setResults(queue);
			
			// Get the pilot IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Course c : queue) {
				IDs.add(new Integer(c.getPilotID()));
				IDs.add(new Integer(c.getInstructorID()));
			}
			
			// Load the data
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.get(uddao.get(IDs)), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/courseQueue.jsp");
		result.setSuccess(true);
	}
}