// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.academy.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CourseAccessControl;

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
			
			// Load documents/exams if its our course
			if (access.getCanComment()) {
				GetDocuments ddao = new GetDocuments(con);
				ctx.setAttribute("docs", ddao.getByCertification(c.getName()), REQUEST);
				
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
			}
			
			// Get Pilot IDs for comments
			Collection<Integer> IDs = new HashSet<Integer>();
			IDs.add(new Integer(c.getPilotID()));
			for (Iterator<CourseComment> i = c.getComments().iterator(); i.hasNext(); ) {
				CourseComment cc = i.next();
				IDs.add(new Integer(cc.getAuthorID()));
			}
			
			// Load Pilot Information
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
			
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