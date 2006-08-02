// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.academy.Course;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;

/**
 * A Web Site Command to display Flight Academy certifications.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CourseListCommand extends AbstractViewCommand {
	
	// Sort options
	private static final String[] SORT_CODE = {"C.STARTDATE", "C.STARTDATE DESC", "LC DESC", "CR.STAGE"};
	private static final List<ComboAlias> SORT_OPTS = ComboUtils.fromArray(new String [] {"Earliest Enrollment", 
			"Latest Enrollment", "Last Comment", "Course Stage"}, SORT_CODE);
	
	// Filtering options
	private static final String[] VIEW_CODE = {"all", "active", "pending", "complete", "unassigned", "mine"};
	private static final List<ComboAlias> VIEW_OPTS = ComboUtils.fromArray(new String[] {"All Courses", 
			"Active Courses", "Pending Enrollments", "Completed Courses", "Unassigned Courses", "My Courses"}, VIEW_CODE);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
        // Get/set start/count parameters
		ViewContext vc = initView(ctx);
		if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
			vc.setSortType(SORT_CODE[0]);
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the courses
			GetAcademyCourses dao = new GetAcademyCourses(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			
			// Filter by status
			Collection<Course> courses = null;
			int filterType = StringUtils.arrayIndexOf(VIEW_CODE, ctx.getParameter("filterType"), 1);
			ctx.setAttribute("filterOpt", VIEW_CODE[filterType], REQUEST);
			switch (filterType) {
				case 0 :
					courses = dao.getAll(vc.getSortType());
					break;
					
				case 1 :
					courses = dao.getByStatus(vc.getSortType(), Course.STARTED);
					break;
					
				case 2:
					courses = dao.getByStatus(vc.getSortType(), Course.PENDING);
					break;
					
				case 4: // Unassigned, which is started+pending
					courses = dao.getByStatus(vc.getSortType(), Course.STARTED);
					courses.addAll(dao.getByStatus(vc.getSortType(), Course.PENDING));
					break;
					
				case 5:
					courses = dao.getByInstructor(ctx.getUser().getID(), vc.getSortType());
					break;
					
				case 3:					
				default:
					courses = dao.getCompleted(0, vc.getSortType());
			}
			
			// Save in the view context
			vc.setResults(courses);
			
			// Get the Pilot IDs - filter out assigned courses since we're iterating anyways
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<Course> i = courses.iterator(); i.hasNext(); ) {
				Course c = i.next();
				if ((filterType == 4) && (c.getInstructorID() != 0))
					i.remove();
				else
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
        ctx.setAttribute("sortTypes", SORT_OPTS, REQUEST);
		ctx.setAttribute("viewOpts", VIEW_OPTS, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/courseList.jsp");
		result.setSuccess(true);
	}
}