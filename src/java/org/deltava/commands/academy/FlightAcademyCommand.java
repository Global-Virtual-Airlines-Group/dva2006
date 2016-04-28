// Copyright 2006, 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.academy.*;
import org.deltava.beans.testing.ExamProfile;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the Flight Academy.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class FlightAcademyCommand extends AbstractAcademyHistoryCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check if we're enabled
		if (!SystemData.getBoolean("academy.enabled"))
			throw securityException("Flight Academy not enabled");
		
		try {
			Connection con = ctx.getConnection();

			// Initialize the Academy History Helper and save if we are in a course
			AcademyHistoryHelper academyHistory = initHistory(ctx.getUser(), con);
			ctx.setAttribute("course", academyHistory.getCurrentCourse(), REQUEST);
			
			// Check if we have enough flights
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getUser().getID());

			// Get all Examination Profiles
			GetExamProfiles epdao = new GetExamProfiles(con);
			Collection<ExamProfile> allExams = epdao.getExamProfiles(true);

			// Check if we have an examination open
			GetExam exdao = new GetExam(con);
			int activeExamID = exdao.getActiveExam(ctx.getUser().getID());

			// Remove all examinations that we have passed or require a higher stage than us
			if (activeExamID != 0) {
				allExams.clear();
				ctx.setAttribute("examActive", Integer.valueOf(activeExamID), REQUEST);
			} else {
				academyHistory.setDebug(ctx.isSuperUser());
				for (Iterator<ExamProfile> i = allExams.iterator(); i.hasNext();) {
					ExamProfile ep = i.next();
					if (!academyHistory.canWrite(ep))
						i.remove();
				}
			}

			// Remove all of the certs that we cannot take
			Collection<Certification> allCerts = new LinkedHashSet<Certification>(academyHistory.getCertifications());
			for (Iterator<Certification> i = allCerts.iterator(); i.hasNext();) {
				Certification cert = i.next();
				if (!academyHistory.canTake(cert))
					i.remove();
			}
			
			// Load Instructors
			Collection<Integer> IDs = academyHistory.getCourses().stream().map(Course::getInstructorID).collect(Collectors.toSet());
			GetUserData uddao = new GetUserData(con);
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("pilots", pdao.get(uddao.get(IDs)), REQUEST);

			// Save the exams and certifications available
			ctx.setAttribute("exams", allExams, REQUEST);
			ctx.setAttribute("certs", allCerts, REQUEST);
			ctx.setAttribute("courses", academyHistory.getCourses(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/flightAcademy.jsp");
		result.setSuccess(true);
	}
}