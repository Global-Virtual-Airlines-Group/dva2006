// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2012, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.time.Instant;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.academy.CourseProgress;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.ExamAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to submit and score Pilot Examinations.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class ExamSubmitCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the examination
			GetExam rdao = new GetExam(con);
			Examination ex = rdao.getExam(ctx.getID());
			if (ex == null)
				throw notFoundException("Invalid Examination - " + ctx.getID());
			
			// Get the user's data
			GetUserData uddao = new GetUserData(con);
			UserData ud = uddao.get(ctx.getUser().getID());

			// Check our access level
			ExamAccessControl access = new ExamAccessControl(ctx, ex, ud);
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
				String answer = ctx.getParameter("answer" + String.valueOf(x));
				if (!StringUtils.isEmpty(answer))
					q.setAnswer(answer);
				
				allMC &= (q instanceof MultiChoiceQuestion);
				if ((q instanceof MultiChoiceQuestion) && (q.getAnswer() != null)) {
					String ca = q.getAnswer().replace("\r", "");
					q.setCorrect(ca.equals(q.getCorrectAnswer()));
					if (q.isCorrect())
						score++;
				}
			}

			ex.setSubmittedOn(Instant.now());

			// If we're entirely multiple choice, then mark the examination scored
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			if (allMC) {
				ex.setScoredOn(ex.getSubmittedOn());
				ex.setStatus(TestStatus.SCORED);
				ex.setScore(score);
				ex.setAutoScored(true);
				ex.setScorerID(ctx.getUser().getID());
				ex.setComments("Automatically Scored Multiple-Choice Examination");
				
				// Set status attributes
		        ctx.setAttribute("isScore", Boolean.TRUE, REQUEST);
		        ctx.setAttribute("autoScore", Boolean.TRUE, REQUEST);

				// Check if we've passed the examination
				ex.setPassFail(score >= ep.getPassScore());

				// Get the Pilot profile
				Pilot usr = pdao.get(ex.getAuthorID());
				ctx.setAttribute("pilot", usr, REQUEST);
			} else {
				ex.setStatus(TestStatus.SUBMITTED);
				ctx.setAttribute("isSubmit", Boolean.TRUE, REQUEST);
				
				// Check if we need to notify anyone
				if (ep.getNotify()) {
					Collection<? extends EMailAddress> scorers = null;
					if (ep.getScorerIDs().isEmpty())
						scorers = pdao.getByRole("Examination", ep.getOwner().getDB());						
					else {
						UserDataMap udm = uddao.get(ep.getScorerIDs());
						scorers = pdao.get(udm).values();
					}
					
					// Build the message context
					MessageContext mctxt = new MessageContext();
					mctxt.addData("user", ctx.getUser());
					mctxt.addData("exam", ex);
					
					// Get the template
					GetMessageTemplate mtdao = new GetMessageTemplate(con);
					mctxt.setTemplate(mtdao.get("EXAMSUBMIT"));
					
					// Send the message
					Mailer mailer = new Mailer(ctx.getUser());
					mailer.setContext(mctxt);
					mailer.send(scorers);
				}
			}
			
			// Start transaction
			ctx.startTX();
			
			// If it's auto-scored and an academy exam, check if it matches any requirements
			if (ex.getAutoScored() && ep.getAcademy() && ex.getPassFail()) {
				GetAcademyProgress apdao = new GetAcademyProgress(con);
				SetAcademy apwdao = new SetAcademy(con);
				Collection<CourseProgress> progs = apdao.getRequirements(ex.getName(), ex.getAuthorID());
				for (CourseProgress cp : progs)
					apwdao.complete(cp.getCourseID(), cp.getID());
			}

			// Write the examination to the database and commit
			SetExam wdao = new SetExam(con);
			wdao.update(ex);
			wdao.updateStats(ex);
			ctx.commitTX();

			// Save the exam to the request
			ctx.setAttribute("exam", ex, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/testing/examUpdate.jsp");
		result.setSuccess(true);
	}
}