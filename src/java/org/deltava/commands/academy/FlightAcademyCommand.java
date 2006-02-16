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
			initHistory(ctx.getUser(), con);
			ctx.setAttribute("course", _academyHistory.getCurrentCourse(), REQUEST);
			
			// Get all Examination Profiles
			GetExamProfiles epdao = new GetExamProfiles(con);
			Collection<ExamProfile> allExams = epdao.getExamProfiles();

			// Remove all examinations that we have passed or require a higher stage than us
			_academyHistory.setDebug(ctx.isSuperUser());
			for (Iterator<ExamProfile> i = allExams.iterator(); i.hasNext();) {
				ExamProfile ep = i.next();
				if (!_academyHistory.canWrite(ep))
					i.remove();
			}
			
			// Remove all of the certs that we cannot take
			for (Iterator<Certification> i = _allCerts.iterator(); i.hasNext(); ) {
				Certification cert = i.next();
				if (!_academyHistory.canTake(cert))
					i.remove();
			}
			
			// Save the exams and certifications available
			ctx.setAttribute("exams", allExams, REQUEST);
			ctx.setAttribute("certs", _allCerts, REQUEST);
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