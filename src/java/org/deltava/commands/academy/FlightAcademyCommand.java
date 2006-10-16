// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.academy.*;
import org.deltava.beans.testing.ExamProfile;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display the Flight Academy.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightAcademyCommand extends AbstractAcademyHistoryCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();

			// Initialize the Academy History Helper and save if we are in a course
			AcademyHistoryHelper academyHistory = initHistory(ctx.getUser(), con);
			ctx.setAttribute("course", academyHistory.getCurrentCourse(), REQUEST);

			// Get all Examination Profiles
			GetExamProfiles epdao = new GetExamProfiles(con);
			Collection<ExamProfile> allExams = epdao.getExamProfiles(true);

			// Check if we have an examination open
			GetExam exdao = new GetExam(con);
			int activeExamID = exdao.getActiveExam(ctx.getUser().getID());

			// Remove all examinations that we have passed or require a higher stage than us
			if (activeExamID != 0) {
				allExams.clear();
				ctx.setAttribute("examActive", new Integer(activeExamID), REQUEST);
			} else {
				academyHistory.setDebug(ctx.isSuperUser());
				for (Iterator<ExamProfile> i = allExams.iterator(); i.hasNext();) {
					ExamProfile ep = i.next();
					if (!academyHistory.canWrite(ep))
						i.remove();
				}
			}

			// Remove all of the certs that we cannot take
			Collection<Certification> allCerts = new ArrayList<Certification>(academyHistory.getCertifications());
			for (Iterator<Certification> i = allCerts.iterator(); i.hasNext();) {
				Certification cert = i.next();
				if (!academyHistory.canTake(cert))
					i.remove();
			}

			// Save the exams and certifications available
			ctx.setAttribute("exams", allExams, REQUEST);
			ctx.setAttribute("certs", allCerts, REQUEST);
			ctx.setAttribute("courses", academyHistory.getCourses(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save pilot name
		ctx.setAttribute("pilot", ctx.getUser(), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/flightAcademy.jsp");
		result.setSuccess(true);
	}
}