// Copyright 2006, 2009, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.academy.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.comparators.*;
import org.deltava.dao.*;

import org.deltava.util.*;

/**
 * A Web Site Command to display Flight Academy certifications.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class CourseListCommand extends AbstractViewCommand {

	// Sort options
	private static final String[] SORT_CODE = { "C.STARTDATE", "C.STARTDATE DESC", "LC DESC", "CR.STAGE" };
	private static final List<ComboAlias> SORT_OPTS = ComboUtils.fromArray(new String[] { "Earliest Enrollment", "Latest Enrollment", "Last Comment", "Course Stage" }, SORT_CODE);

	// Filtering options
	private static final String[] VIEW_CODE = { "all", "active", "pending", "complete", "unassigned", "mine" };
	private static final List<ComboAlias> VIEW_OPTS = ComboUtils.fromArray(new String[] { "All Courses", "Active Courses", "Pending Enrollments", "Completed Courses", "Unassigned Courses", "My Courses" }, VIEW_CODE);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get/set start/count parameters
		ViewContext<Course> vc = initView(ctx, Course.class);
		if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
			vc.setSortType(SORT_CODE[0]);

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the courses
			GetAcademyCourses dao = new GetAcademyCourses(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());

			// Load the instructors
			GetUserData uddao = new GetUserData(con);
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			List<String> codes = new ArrayList<String>(Arrays.asList(VIEW_CODE));
			Collection<Pilot> instructors = new TreeSet<Pilot>(new PilotComparator(PersonComparator.FIRSTNAME));
			for (AirlineInformation ai : uddao.getAirlines(true).values())
				instructors.addAll(pdao.getByRole("Instructor", ai.getDB()));
			instructors.forEach(in -> codes.add(in.getComboAlias()));

			// Filter by status
			int filterType = codes.indexOf(ctx.getParameter("filterType"));
			if (filterType == -1)
				filterType = 1;
			
			ctx.setAttribute("filterOpt", codes.get(filterType), REQUEST);
			switch (filterType) {
				case 0:
					vc.setResults(dao.getAll(vc.getSortType()));
					break;

				case 1:
					vc.setResults(dao.getByStatus(Status.STARTED, vc.getSortType(), null));
					break;

				case 2:
					ctx.setAttribute("isPending", Boolean.TRUE, REQUEST);
					vc.setResults(dao.getByStatus(Status.PENDING, vc.getSortType(), null));
					break;

				case 4: // Unassigned
					vc.setResults(dao.getByInstructor(0, vc.getSortType()));
					break;

				case 5:
					vc.setResults(dao.getByInstructor(ctx.getUser().getID(), vc.getSortType()));
					break;

				case 3:
					vc.setResults(dao.getCompleted(0, vc.getSortType()));
					break;

				default:
					vc.setResults(dao.getByInstructor(Integer.parseInt(ctx.getParameter("filterType")), vc.getSortType()));
			}

			// Load the Pilot profiles
			Collection<Integer> IDs = vc.getResults().stream().map(Course::getPilotID).collect(Collectors.toSet());
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("userData", udm, REQUEST);
			ctx.setAttribute("pilots", pdao.get(udm), REQUEST);

			// Save view options
			Collection<ComboAlias> viewOpts = new ArrayList<ComboAlias>(VIEW_OPTS);
			viewOpts.addAll(instructors);
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