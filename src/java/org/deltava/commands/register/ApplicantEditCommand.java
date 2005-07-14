// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.register;

import java.sql.Connection;

import org.deltava.beans.Applicant;

import org.deltava.commands.*;

import org.deltava.dao.GetApplicant;
import org.deltava.dao.DAOException;

import org.deltava.security.command.ApplicantAccessControl;

/**
 * A Web Site Command to edit Applicant Profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ApplicantEditCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Applicant
			GetApplicant dao = new GetApplicant(con);
			Applicant a = dao.get(ctx.getID());
			if (a == null)
				throw new CommandException("Invalid Applicant - " + ctx.getID());

			// Check our access level
			ApplicantAccessControl access = new ApplicantAccessControl(ctx, a);
			access.validate();
			if (!access.getCanEdit())
				throw new CommandSecurityException("Cannot edit Applicant");

			// Save the applicant and the access controller
			ctx.setAttribute("applicant", a, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/register/applicantEdit.jsp");
		result.setSuccess(true);
	}
}