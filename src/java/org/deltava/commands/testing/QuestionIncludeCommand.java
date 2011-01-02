// Copyright 2007, 2008, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.QuestionProfileAccessControl;

import org.deltava.util.CollectionUtils;

/**
 * A Web Site Command to update the Examinations a Question appears in, if the current web
 * application is not the owner of the Question.
 * @author Luke
 * @version 3.5
 * @since 2.0
 */

public class QuestionIncludeCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the question profile
			GetExamQuestions rdao = new GetExamQuestions(con);
			QuestionProfile qp = rdao.getQuestionProfile(ctx.getID());
			if (qp == null)
				throw notFoundException("Invalid Question Profile - " + ctx.getID());
			
			// Validate our access
			QuestionProfileAccessControl access = new QuestionProfileAccessControl(ctx, qp);
			access.validate();
			if (!access.getCanInclude())
				throw securityException("Cannot modify Question Profile examination list");
			
			// Get our airline's exam names
			GetExamProfiles epdao = new GetExamProfiles(con);
			Collection<String> ourExamNames = new HashSet<String>();
			for (ExamProfile ep : epdao.getExamProfiles())
				ourExamNames.add(ep.getName());
			
			// Load the exam names from the request
			Collection<String> examNames = ctx.getParameters("examNames");
			
			// Ensure that we have only selected exams in our airline
			examNames.removeAll(CollectionUtils.getDelta(examNames, ourExamNames));
			
			// Update the exams - remove all from our airline, then re-add what we have left
			Collection<String> exams = qp.getExams();
			exams.removeAll(ourExamNames);
			exams.addAll(examNames);
			qp.setExams(exams);
			
			// Save the question profile
			SetExamProfile wdao = new SetExamProfile(con);
			wdao.writeExams(qp);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/testing/profileUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}