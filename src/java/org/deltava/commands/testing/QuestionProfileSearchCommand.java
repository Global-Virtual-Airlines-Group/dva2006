// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to search Examination Questions for specific text. 
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class QuestionProfileSearchCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check for search string
		String searchStr = ctx.getParameter("searchStr");
		CommandResult result = ctx.getResult();
		if (searchStr == null) {
			result.setURL("/jsp/testing/questionSearch.jsp");
			result.setSuccess(true);
			return;
		}
		
		try {
			GetExamQuestions qdao = new GetExamQuestions(ctx.getConnection());
			ctx.setAttribute("results", qdao.search(searchStr), REQUEST);
			ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally { 
			ctx.release();
		}
		
		// Forward to the JSP
		result.setURL("/jsp/testing/questionSearch.jsp");
		result.setSuccess(true);
	}
}