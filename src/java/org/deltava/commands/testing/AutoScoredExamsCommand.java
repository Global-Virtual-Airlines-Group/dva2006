// Copyright 2007, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.testing.Examination;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display automatically scored Examinations.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class AutoScoredExamsCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view context and examination name filter
		ViewContext<Examination> vc = initView(ctx, Examination.class);
		String examName = ctx.getParameter("examName");
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the exam names
			GetExam dao = new GetExam(con);
			Collection<String> examNames = dao.getAutoScoredExamNames();
			ctx.setAttribute("examNames", examNames, REQUEST);
			if (StringUtils.isEmpty(examName) || (!examNames.contains(examName)))
				examName = null;

			// Get the exams
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getAutoScored(examName));
			
			// Load the Pilots
			Collection<Integer> IDs = vc.getResults().stream().map(Examination::getAuthorID).collect(Collectors.toSet());
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/testing/autoScoredExams.jsp");
		result.setSuccess(true);
	}
}