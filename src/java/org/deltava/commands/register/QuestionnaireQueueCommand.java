// Copyright 2005, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.register;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.testing.Examination;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to view pending Applicant Questionnaires.
 * @author Luke
 * @version 5.0
 * @since 1.0
 */

public class QuestionnaireQueueCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<Examination> vctxt = initView(ctx, Examination.class);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the questionnaire queue
			GetQuestionnaire dao = new GetQuestionnaire(con);
			dao.setQueryStart(vctxt.getStart());
			dao.setQueryMax(vctxt.getCount());
			vctxt.setResults(dao.getPending());
			
			// Save the applicants in the request
			Collection<Integer> IDs = vctxt.getResults().stream().map(Examination::getAuthorID).collect(Collectors.toSet());
			GetApplicant adao = new GetApplicant(con);
			ctx.setAttribute("applicants", adao.getByID(IDs, "APPLICANTS"), REQUEST);
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