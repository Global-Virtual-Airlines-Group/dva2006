// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.academy.Course;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ExamAccessControl;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to view Check Ride records.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CheckRideCommand extends AbstractCommand {

	private static final List<ComboAlias> PASS_FAIL = ComboUtils.fromArray(new String[] { "PASS", "UNSATISFACTORY" },
			new String[] { "true", "false" });

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result
		CommandResult result = ctx.getResult();

		// Check if we are attempting to rescore
		boolean isRescore = "edit".equals(ctx.getCmdParameter(OPERATION, null));
		try {
			Connection con = ctx.getConnection();

			// Load the check ride data
			GetExam dao = new GetExam(con);
			CheckRide cr = dao.getCheckRide(ctx.getID());
			if (cr == null)
				throw notFoundException("Invalid Check Ride - " + ctx.getID());

			// Get the pilot taking the checkride
			GetUserData uddao = new GetUserData(con);
			UserData ud = uddao.get(cr.getPilotID());

			// Check our access
			ExamAccessControl access = new ExamAccessControl(ctx, cr, ud);
			access.validate();
			if (!access.getCanRead())
				throw securityException("Cannot view Check Ride");

			// Load Flight Academy data
			if (cr.getAcademy()) {
				Collection<Integer> ids = new HashSet<Integer>();
				ids.add(new Integer(cr.getID()));

				// Get the DAO and the course
				GetAcademyCourses acdao = new GetAcademyCourses(con);
				List<Course> courses = new ArrayList<Course>(acdao.getByCheckRide(ids));
				if (!courses.isEmpty())
					ctx.setAttribute("course", courses.get(0), REQUEST);
			}

			// Load the pilot data
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilot", pdao.get(ud), REQUEST);
			if (cr.getScorerID() != 0) {
				ctx.setAttribute("scorer", pdao.get(uddao.get(cr.getScorerID())), REQUEST);
				ctx.setAttribute("score", String.valueOf(cr.getPassFail()), REQUEST);
			}

			// If the checkride has been submitted, get the flight report
			if (cr.getFlightID() != 0) {
				GetFlightReports frdao = new GetFlightReports(con);
				ctx.setAttribute("pirep", frdao.getACARS(ud.getDB(), cr.getFlightID()), REQUEST);
			}

			// Save the checkride and the access controller
			ctx.setAttribute("checkRide", cr, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
			ctx.setAttribute("passFail", PASS_FAIL, REQUEST);

			// Check if we can score/edit or not
			if (cr.getStatus() == Test.SCORED)
				result.setURL(isRescore && access.getCanEdit() ? "/jsp/testing/cRideScore.jsp" : "/jsp/testing/cRideRead.jsp");
			else
				result.setURL(access.getCanScore() ? "/jsp/testing/cRideScore.jsp" : "/jsp/testing/cRideRead.jsp");
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		result.setSuccess(true);
	}
}