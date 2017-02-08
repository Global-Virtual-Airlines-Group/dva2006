// Copyright 2006, 2009, 2010, 2011, 2012, 2014, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.academy.*;
import org.deltava.beans.system.AirlineInformation;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.comparators.*;
import org.deltava.dao.*;

import org.deltava.security.command.CourseAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display a Fleet Academy course.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class CourseCommand extends AbstractAcademyHistoryCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the Course
			GetAcademyCourses cdao = new GetAcademyCourses(con);
			Course c = cdao.get(ctx.getID());
			if (c == null)
				throw notFoundException("Invalid Course - " + ctx.getID());

			// Load the user
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(uddao.get(c.getPilotID()));
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + c.getPilotID());
			
			// Init the history and fully populate the Course with check ride
			AcademyHistoryHelper helper = initHistory(p, con);
			helper.setDebug(ctx.isSuperUser());
			Course c2 = helper.getCourse(c.getID());
			c2.getCheckRides().forEach(cr -> c.addCheckRide(cr));

			// Get the certification profile
			Certification cert = helper.getCertification(c.getCode());
			if (cert == null)
				throw notFoundException("Invalid Certification - " + c.getName());
			
			// Check our access
			CourseAccessControl access = new CourseAccessControl(ctx, c);
			access.setCertification(cert);
			access.validate();
			
			// Get Pilot IDs from comments/progress
			Collection<Integer> IDs = new HashSet<Integer>();
			IDs.add(Integer.valueOf(c.getPilotID()));
			c.getComments().forEach(cc -> IDs.add(Integer.valueOf(cc.getAuthorID())));
			c.getProgress().forEach(cp -> IDs.add(Integer.valueOf(cp.getAuthorID())));
			if (c.getInstructorID() != 0)
				IDs.add(Integer.valueOf(c.getInstructorID()));
			
			// Load documents/exams if its our course
			if (access.getCanComment()) {
				GetDocuments ddao = new GetDocuments(con);
				ctx.setAttribute("docs", ddao.getByCertification(SystemData.get("airline.db"), c.getCode()), REQUEST);
				
				// Get videos
				GetVideos vdao = new GetVideos(con);
				ctx.setAttribute("videos", vdao.getVideos(c.getCode()), REQUEST);
				
				// Show exam status
				Collection<Test> exams = new TreeSet<Test>();
				for (Test t : helper.getExams()) {
					if ((t instanceof Examination) && (cert.getExamNames().contains(t.getName())))
						exams.add(t);
					else if (t instanceof CheckRide) {
						CheckRide cr = (CheckRide) t;
						if (cr.getCourseID() == c.getID())
							exams.add(t);
					}
				}
				
				// Save examination status
				ctx.setAttribute("exams", exams, REQUEST);
				
				// Get instruction flight log
				GetAcademyCalendar fdao = new GetAcademyCalendar(con);
				Collection<? extends Instruction> flights = fdao.getFlights(c.getID());
				Collection<? extends Instruction> sessions = fdao.getSessions(c.getID());
				ctx.setAttribute("sessions", sessions, REQUEST);
				ctx.setAttribute("flights", flights, REQUEST);
				
				// Get Pilot IDs from flights
				Collection<Instruction> insC = new ArrayList<Instruction>(flights);
				insC.addAll(sessions);
				insC.forEach(ins -> IDs.add(Integer.valueOf(ins.getInstructorID())));
			}
			
			// Load Pilot Information
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("userData", udm, REQUEST);
			ctx.setAttribute("pilots", pdao.get(udm), REQUEST);
			
			// If we can reassign, load instructors
			if (access.getCanAssignInstructor()) {
				GetPilotDirectory prdao = new GetPilotDirectory(con);
				Collection<Pilot> instructors = new TreeSet<Pilot>(new PilotComparator(PersonComparator.FIRSTNAME));
				for (AirlineInformation ai : uddao.getAirlines(true).values())
					instructors.addAll(prdao.getByRole("Instructor", ai.getDB()));
				
				ctx.setAttribute("instructors", instructors, REQUEST);
			}
			
			// Save the course and the access controller
			ctx.setAttribute("course", c, REQUEST);
			ctx.setAttribute("cert", cert, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/courseView.jsp");
		result.setSuccess(true);
	}
}