// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

/**
 * A Web Site Command to create a new Pilot Examination.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ExamCreateCommand extends AbstractTestHistoryCommand {

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

		Examination ex = null;
		try {
			Connection con = ctx.getConnection();

			// Get the examination profile
			GetExamProfiles epdao = new GetExamProfiles(con);
			ExamProfile ep = epdao.getExamProfile(examName);
			if (ep == null)
				throw new CommandException("Invalid Examination - " + examName);

			// Get the examinations for this user
			Pilot usr = (Pilot) ctx.getUser();
			GetExam exdao = new GetExam(con);
			List exams = exdao.getExams(usr.getID());

			// Check if we already this examination in a passed or pending state
			for (Iterator i = exams.iterator(); i.hasNext();) {
				Test t = (Test) i.next();
				boolean isComplete = ((t.getStatus() == Test.SCORED) && t.getPassFail());
				if ((t.getName().equals(examName)) && ((t.getStatus() != Test.SCORED) || isComplete))
					throw securityException("Cannot re-take " + examName + " examination");
			}
            
            // Initialize the testing history helper
            initTestHistory(usr, con);

			// Check if we can take the exam
            if (!_testHistory.canWrite(ep))
				throw securityException("Cannot take " + examName);

			// Get the Message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("EXAMCREATE"));

			// Create the examination
			ex = new Examination(examName);
			ex.setPilotID(usr.getID());
			ex.setStage(ep.getStage());
			ex.setStatus(Test.NEW);

			// Set the creation/expiration date/times
			Calendar cld = Calendar.getInstance();
			ex.setDate(cld.getTime());
			cld.add(Calendar.MINUTE, ep.getTime());
			ex.setExpiryDate(cld.getTime());

			// Load the question pool for this examination
			epdao.setQueryMax(ep.getSize());
			List qPool = epdao.getQuestionPool(examName, true);
			if (qPool.isEmpty())
				throw new CommandException("Empty Question Pool for " + examName);

			// Add the questions to the exam
			int qNum = 0;
			for (Iterator i = qPool.iterator(); i.hasNext();) {
				QuestionProfile qp = (QuestionProfile) i.next();
				Question q = new Question(qp);
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
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setURL("exam", null, ex.getID());
		result.setSuccess(true);
	}
}