// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.register;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.testing.Examination;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to view pending Applicant Questionnaires.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class QuestionnaireQueueCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the questionnaire queue
			GetQuestionnaire dao = new GetQuestionnaire(con);
			List<Examination> queue = dao.getPending();
			
			// Build a collection of applicant IDs
			Set<Integer> applicantIDs = new HashSet<Integer>();
			for (Iterator<Examination> i = queue.iterator(); i.hasNext(); ) {
				Examination exam = i.next();
				applicantIDs.add(new Integer(exam.getPilotID()));
			}
			
			// Get the DAO and save the applicant IDs in the request
			GetApplicant adao = new GetApplicant(con);
			ctx.setAttribute("applicants", adao.getByID(applicantIDs, "APPLICANTS"), REQUEST);
			
			// Save the questionnaire queue
			ctx.setAttribute("examQueue", queue, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/register/questionnaireQueue.jsp");
		result.setSuccess(true);
	}
}