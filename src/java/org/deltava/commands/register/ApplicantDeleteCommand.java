// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.register;

import java.sql.Connection;

import org.deltava.beans.Applicant;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ApplicantAccessControl;

/**
 * A Web Site Command to delete an Applicant profile.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ApplicantDeleteCommand extends AbstractCommand {

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
				throw notFoundException("Invalid Applicant - " + ctx.getID());
			
			// Check our access level
			ApplicantAccessControl access = new ApplicantAccessControl(ctx, a);
			access.validate();
			if (!access.getCanDelete())
				throw securityException("Cannot Delete Applicant");
			
			// Save the Applicant in the request
			ctx.setAttribute("applicant", a, REQUEST);
			
			// Start a transaction
			ctx.startTX();
			
			// Delete the Applicant Profile
			SetApplicant adao = new SetApplicant(con);
			adao.delete(a.getID());
			
			// Delete the User Data record
			SetUserData uddao = new SetUserData(con);
			uddao.delete(a.getID());
			
			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status attribute
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/register/applicantReject.jsp");
		result.setSuccess(true);
	}
}