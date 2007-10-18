// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 2.0
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
			GetExamProfiles rdao = new GetExamProfiles(con);
			QuestionProfile qp = rdao.getQuestionProfile(ctx.getID());
			if (qp == null)
				throw notFoundException("Invalid Question Profile - " + ctx.getID());
			
			// Validate our access
			QuestionProfileAccessControl access = new QuestionProfileAccessControl(ctx, qp);
			access.validate();
			if (!access.getCanInclude())
				throw securityException("Cannot modify Question Profile examination list");
			
			// Load the exams from the request
			Collection<String> examNames = ctx.getParameters("examNames");
			qp.setExams((examNames != null) ? examNames : new HashSet<String>());
			
			// Get our airline's exam names
			Collection<String> ourExams = new LinkedHashSet<String>(); 
			for (Iterator<ExamProfile> i = rdao.getExamProfiles().iterator(); i.hasNext(); ) {
				ExamProfile ep = i.next();
				ourExams.add(ep.getName());
			}
			
			// Ensure that we have only selected exams in our airline
			examNames.removeAll(CollectionUtils.getDelta(examNames, ourExams));
			
			// Update the exams - remove all from our airline, then re-add what we have left
			Collection<String> exams = qp.getExamNames();
			exams.removeAll(ourExams);
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
		result.setType(CommandResult.REQREDIRECT);
		result.setSuccess(true);
	}
}