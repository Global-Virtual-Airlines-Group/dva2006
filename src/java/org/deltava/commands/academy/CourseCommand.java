// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.academy.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CourseAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display a Fleet Academy course.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CourseCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
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
			ctx.setAttribute("isComplete", Boolean.valueOf(helper.hasCompleted(c.getName())), REQUEST);
			
			// Get the certification profile
			Certification cert = cdao.get(c.getName());
			if (cert == null)
				throw notFoundException("Invalid Certification - " + c.getName());
			
			// Check our access
			CourseAccessControl access = new CourseAccessControl(ctx, c);
			access.validate();
			
			// Get Pilot IDs from comments
			Collection<Integer> IDs = new HashSet<Integer>();
			IDs.add(new Integer(c.getPilotID()));
			IDs.add(new Integer(c.getInstructorID()));
			for (Iterator<CourseComment> i = c.getComments().iterator(); i.hasNext(); ) {
				CourseComment cc = i.next();
				IDs.add(new Integer(cc.getAuthorID()));
			}
			
			// Get Pilot IDs from progress
			for (Iterator<CourseProgress> i = c.getProgress().iterator(); i.hasNext(); ) {
				CourseProgress cp = i.next();
				IDs.add(new Integer(cp.getAuthorID()));
			}
			
			// Load documents/exams if its our course
			if (access.getCanComment()) {
				GetDocuments ddao = new GetDocuments(con);
				ctx.setAttribute("docs", ddao.getByCertification(c.getName()), REQUEST);
				
				// Get videos
				GetVideos vdao = new GetVideos(con);
				ctx.setAttribute("videos", vdao.getVideos(c.getName()), REQUEST);
				
				// Show exam status
				Collection<Test> exams = new TreeSet<Test>();
				for (Iterator<Test> i = helper.getExams().iterator(); i.hasNext(); ) {
					Test t = i.next();
					if (cert.getExamNames().contains(t.getName())) {
						exams.add(t);
					} else if ((t instanceof CheckRide) && (t.getName().startsWith(cert.getName()))) {
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
				for (Iterator<? extends Instruction> i = insC.iterator(); i.hasNext(); ) {
					Instruction ins = i.next();
					IDs.add(new Integer(ins.getInstructorID()));
				}
			}
			
			// Load Pilot Information
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
			
			// If we can reassign, load instructors
			if (access.getCanAssign()) {
				GetPilotDirectory prdao = new GetPilotDirectory(con);
				ctx.setAttribute("instructors", prdao.getByRole("Instructor", SystemData.get("airline.db")), REQUEST);
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