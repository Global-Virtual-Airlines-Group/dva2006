// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.academy.AcademyHistoryHelper;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to create new Flight Academy Examinations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ExamCreateCommand extends AbstractAcademyHistoryCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the exam name
		String examName = ctx.getParameter("examName");

		// Create the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());
		
		// Get command results
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);

		Examination ex = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the examination profile
			GetExamProfiles epdao = new GetExamProfiles(con);
			ExamProfile ep = epdao.getExamProfile(examName);
			if (ep == null)
				throw notFoundException("Invalid Examination - " + examName);
			
			// Load the test history
			AcademyHistoryHelper academyHistory = initHistory(ctx.getUser(), con);
			boolean examsLocked = academyHistory.isLockedOut(SystemData.getInt("testing.lockout"));
			if (examsLocked)
				throw securityException("Testing Center locked out");
			
			// Check if we already have an exam open
			GetExam exdao = new GetExam(con);
			int activeExamID = exdao.getActiveExam(ctx.getUser().getID());
			if (activeExamID != 0) {
				ex = exdao.getExam(activeExamID);
				if ((ex != null) && (ex.getStatus() == Test.NEW)) {
					result.setURL("exam", null, ex.getID());
					result.setSuccess(true);
					return;
				}

				throw securityException("Already have Examination pending");
			}
			
			// Check if we can take the exam
            if (!academyHistory.canWrite(ep))
				throw securityException("Cannot take " + examName);

			// Get the Message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("EXAMCREATE"));

			// Create the examination
			ex = new Examination(examName);
			ex.setOwner(ep.getOwner());
			ex.setPilotID(ctx.getUser().getID());
			ex.setStage(ep.getStage());
			ex.setStatus(Test.NEW);
			ex.setAcademy(true);
			
			// Set the creation/expiration date/times
			Calendar cld = Calendar.getInstance();
			ex.setDate(cld.getTime());
			cld.add(Calendar.MINUTE, ep.getTime());
			ex.setExpiryDate(cld.getTime());

			// Load the question pool for this examination
			epdao.setQueryMax(ep.getSize());
			Collection<QuestionProfile> qPool = epdao.getQuestionPool(examName, true, true);
			if (qPool.isEmpty()) {
				CommandException ce = new CommandException("Empty Question Pool for " + examName);
				ce.setLogStackDump(false);
				throw ce;
			}

			// Add the questions to the exam
			int qNum = 0;
			for (Iterator<QuestionProfile> i = qPool.iterator(); i.hasNext();) {
				QuestionProfile qp = i.next();
				Question q = null;
				if (qp instanceof MultipleChoice) {
					q = new MultiChoiceQuestion((MultiChoiceQuestionProfile) qp);
				} else {
					q = new Question(qp);
				}
				
				q.setNumber(++qNum);
				ex.addQuestion(q);
			}

			// Get the DAO and write the exam to the database
			SetExam wdao = new SetExam(con);
			wdao.write(ex);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Send notification message
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctxt);
		mailer.send(ctx.getUser());

		// Forward to the Examination Command
		result.setURL("exam", null, ex.getID());
		result.setSuccess(true);
	}
}