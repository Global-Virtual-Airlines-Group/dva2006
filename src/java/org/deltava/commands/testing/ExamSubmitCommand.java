// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.ExamAccessControl;

/**
 * A Web Site Command to submit and score Pilot Examinations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ExamSubmitCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the examination
			GetExam rdao = new GetExam(con);
			Examination ex = rdao.getExam(ctx.getID());
			if (ex == null)
				throw notFoundException("Invalid Examination - " + ctx.getID());

			// Check our access level
			ExamAccessControl access = new ExamAccessControl(ctx, ex);
			access.validate();
			if (!access.getCanSubmit())
				throw securityException("Cannot submit Examination");

			// Load the examination profile
			GetExamProfiles epdao = new GetExamProfiles(con);
			ExamProfile ep = epdao.getExamProfile(ex.getName());
			if (ep == null)
				throw notFoundException("Cannot load Examination Profile - " + ex.getName());

			// Save answers from the request - score if it's a multiple choice question
			int score = 0;
			boolean allMC = true;
			for (int x = 1; x <= ex.getSize(); x++) {
				Question q = ex.getQuestion(x);
				q.setAnswer(ctx.getParameter("answer" + String.valueOf(x)));
				allMC &= (q instanceof MultiChoiceQuestion);
				if (q instanceof MultiChoiceQuestion) {
					q.setCorrect(q.getAnswer().equals(q.getCorrectAnswer()));
					if (q.isCorrect())
						score++;
				}
			}

			// Set the status of the examination, and submitted date
			Calendar cld = Calendar.getInstance();
			ex.setSubmittedOn(cld.getTime());

			// If we're entirely multiple choice, then mark the examination scored
			if (allMC) {
				ex.setScoredOn(cld.getTime());
				ex.setStatus(Test.SCORED);
				ex.setScore(score);
				ex.setScorerID(ctx.getUser().getID());
				ex.setComments("Automatically Scored Multiple-Choice Examination");
				
				// Set status attributes
		        ctx.setAttribute("isScore", Boolean.TRUE, REQUEST);
		        ctx.setAttribute("autoScore", Boolean.TRUE, REQUEST);

				// Create the messaging context
				MessageContext mctxt = new MessageContext();
				mctxt.addData("user", ctx.getUser());

				// Check if we've passed the examination
				ex.setPassFail(score >= ep.getPassScore());
				mctxt.addData("result", ex.getPassFail() ? "PASS" : "UNSATISFACTORY");

				// Get the Message template
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("EXAMSCORE"));
				mctxt.addData("exam", ex);

				// Get the Pilot profile
				GetPilot pdao = new GetPilot(con);
				Pilot usr = pdao.get(ex.getPilotID());
				ctx.setAttribute("pilot", usr, REQUEST);
				mctxt.addData("pilot", usr);

				// Send the notification message
				Mailer mailer = new Mailer(ctx.getUser());
				mailer.setContext(mctxt);
				mailer.send(usr);
			} else {
				ex.setStatus(Test.SUBMITTED);
				ctx.setAttribute("isSubmit", Boolean.TRUE, REQUEST);
			}

			// Write the examination to the database
			SetExam wdao = new SetExam(con);
			wdao.update(ex);

			// Save the exam to the request
			ctx.setAttribute("exam", ex, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/testing/examUpdate.jsp");
		result.setSuccess(true);
	}
}