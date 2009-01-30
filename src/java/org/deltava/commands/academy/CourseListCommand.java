// Copyright 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.academy.Course;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Flight Academy certifications.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class CourseListCommand extends AbstractViewCommand {

	// Sort options
	private static final String[] SORT_CODE = { "C.STARTDATE", "C.STARTDATE DESC", "LC DESC", "CR.STAGE" };
	private static final List<ComboAlias> SORT_OPTS = ComboUtils.fromArray(new String[] { "Earliest Enrollment",
			"Latest Enrollment", "Last Comment", "Course Stage" }, SORT_CODE);

	// Filtering options
	private static final String[] VIEW_CODE = { "all", "active", "pending", "complete", "unassigned", "mine" };
	private static final List<ComboAlias> VIEW_OPTS = ComboUtils.fromArray(new String[] { "All Courses",
			"Active Courses", "Pending Enrollments", "Completed Courses", "Unassigned Courses", "My Courses" },
			VIEW_CODE);

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

			// Load the instructors
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			List<String> codes = new ArrayList<String>(Arrays.asList(VIEW_CODE));
			Collection<Pilot> insList = pdao.getByRole("Instructor", SystemData.get("airline.db"));
			for (Iterator<? extends ComboAlias> i = insList.iterator(); i.hasNext();) {
				ComboAlias a = i.next();
				codes.add(a.getComboAlias());
			}

			// Filter by status
			Collection<Course> courses = null;
			int filterType = codes.indexOf(ctx.getParameter("filterType"));
			if (filterType == -1)
				filterType = 1;
			
			ctx.setAttribute("filterOpt", codes.get(filterType), REQUEST);
			switch (filterType) {
				case 0:
					courses = dao.getAll(vc.getSortType());
					break;

				case 1:
					courses = dao.getByStatus(vc.getSortType(), Course.STARTED);
					break;

				case 2:
					ctx.setAttribute("isPending", Boolean.TRUE, REQUEST);
					courses = dao.getByStatus(vc.getSortType(), Course.PENDING);
					break;

				case 4: // Unassigned
					courses = dao.getByInstructor(0, vc.getSortType());
					break;

				case 5:
					courses = dao.getByInstructor(ctx.getUser().getID(), vc.getSortType());
					break;

				case 3:
					courses = dao.getCompleted(0, vc.getSortType());
					break;

				default:
					courses = dao.getByInstructor(Integer.parseInt(ctx.getParameter("filterType")), vc.getSortType());
			}

			// Save in the view context
			vc.setResults(courses);

			// Get the Pilot IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<Course> i = courses.iterator(); i.hasNext(); ) {
				Course c = i.next();
				IDs.add(new Integer(c.getPilotID()));
			}

			// Load the Pilot profiles
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);

			// Save view options
			Collection<ComboAlias> viewOpts = new ArrayList<ComboAlias>(VIEW_OPTS);
			viewOpts.addAll(insList);
			ctx.setAttribute("viewOpts", viewOpts, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save sort options
		ctx.setAttribute("sortTypes", SORT_OPTS, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/courseList.jsp");
		result.setSuccess(true);
	}
}