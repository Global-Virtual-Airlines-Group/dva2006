// Copyright 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.sql.Connection;

import org.deltava.beans.testing.QuestionProfile;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.QuestionProfileAccessControl;

/**
 * A Web Site Command to delete an Examination Question profile.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class QuestionDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Examination profile
			GetExamQuestions rdao = new GetExamQuestions(con);
			QuestionProfile qp = rdao.getQuestionProfile(ctx.getID());
			if (qp == null)
				throw notFoundException("Invalid Question Profile - " + ctx.getID());
			
			// Validate our access
			QuestionProfileAccessControl access = new QuestionProfileAccessControl(ctx, qp);
			access.validate();
			if (!access.getCanDelete())
				throw securityException("Cannot delete Examination Question Profile");
			
			// Save the profile in the request
			ctx.setAttribute("question", qp, REQUEST);

			// Get the write DAO and delete the profile
			SetExamProfile wdao = new SetExamProfile(con);
			wdao.delete(qp);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);

		// Foward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/testing/profileUpdate.jsp");
		result.setSuccess(true);
	}
}