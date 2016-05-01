// Copyright 2007, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.register;

import java.util.Collection;

import org.deltava.beans.Applicant;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to find an Applicant based on e-mail address.
 * @author Luke
 * @version 7.0
 * @since 2.0
 */

public class ApplicantFindCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get command results
		CommandResult result = ctx.getResult();
		if (ctx.getParameter("fName") == null) {
			result.setURL("/jsp/register/applicantSearch.jsp");
			result.setSuccess(true);
			return;
		}

		Collection<Applicant> results = null;
		try {
			GetApplicant adao = new GetApplicant(ctx.getConnection());
			results = adao.getByName(ctx.getParameter("fName"), ctx.getParameter("lName"));
		} catch (DAOException de) { 
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save status attributes
		ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
		ctx.setAttribute("applicants", results, REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/register/applicantSearch.jsp");
		result.setSuccess(true);
	}
}