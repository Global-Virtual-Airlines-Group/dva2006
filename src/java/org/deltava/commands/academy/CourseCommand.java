// Copyright 2006, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 3.4
 * @since 1.0
 */

public class CourseCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Course
			GetAcademyCourses dao = new GetAcademyCourses(con);
			Course c = dao.get(ctx.getID());
			if (c == null)
				throw notFoundException("Invalid Course - " + ctx.getID());
			
			// Get our exams and init the academy helper
			GetExam exdao = new GetExam(con);
			GetAcademyCertifications cdao = new GetAcademyCertifications(con);
			AcademyHistoryHelper helper = new AcademyHistoryHelper(dao.getByPilot(c.getPilotID()), cdao.getAll());
			helper.setDebug(ctx.isSuperUser());
			helper.addExams(exdao.getExams(c.getPilotID()));
			
			// Get the certification profile
			Certification cert = cdao.get(c.getName());
			if (cert == null)
				throw notFoundException("Invalid Certification - " + c.getName());
			
			// Load our exams
			List<CheckRide> rides = exdao.getAcademyCheckRides(c.getID());
			c.setCheckRide(rides.isEmpty() ? null : rides.get(0));
			
			// Check our access
			CourseAccessControl access = new CourseAccessControl(ctx, c);
			access.validate();
			
			// Get Pilot IDs from comments/progress
			Collection<Integer> IDs = new HashSet<Integer>();
			IDs.add(new Integer(c.getPilotID()));
			IDs.add(new Integer(c.getInstructorID()));
			for (CourseComment cc : c.getComments())
				IDs.add(new Integer(cc.getAuthorID()));
			for (CourseProgress cp : c.getProgress())
				IDs.add(new Integer(cp.getAuthorID()));
			
			// Load documents/exams if its our course
			if (access.getCanComment()) {
				GetDocuments ddao = new GetDocuments(con);
				ctx.setAttribute("docs", ddao.getByCertification(SystemData.get("airline.db"), c.getCode()), REQUEST);
				
				// Get videos
				GetVideos vdao = new GetVideos(con);
				ctx.setAttribute("videos", vdao.getVideos(c.getName()), REQUEST);
				
				// Show exam status
				Collection<Test> exams = new TreeSet<Test>(rides);
				for (Iterator<Test> i = helper.getExams().iterator(); i.hasNext(); ) {
					Test t = i.next();
					if ((t.getType() == Test.EXAM) && (cert.getExamNames().contains(t.getName())))
						exams.add(t);
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
				for (Iterator<? extends Instruction> i = insC.iterator(); i.hasNext(); ) {
					Instruction ins = i.next();
					IDs.add(new Integer(ins.getInstructorID()));
				}
			}
			
			// Load Pilot Information
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserDataMap udm = uddao.get(IDs);
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