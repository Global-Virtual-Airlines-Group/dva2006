// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.register;

import java.sql.Connection;

import org.deltava.beans.Applicant;
import org.deltava.beans.system.AddressValidation;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command for Applicants to validate their e-mail address.
 * @author LKolin
 * @version 2.0
 * @since 2.0
 */

public class ValidateEMailCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the command result
		CommandResult result = ctx.getResult();
		try {
			Connection con = ctx.getConnection();
			
			// Get the applicant
			GetApplicant adao = new GetApplicant(con);
			Applicant a = adao.get(ctx.getID());
			if ((a == null) || (a.getStatus() != Applicant.PENDING)) {
				ctx.release();
				
				// Redirect to the applicant find page
				result.setURL("/jsp/register/applicantSearch.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Save the applicant
			ctx.setAttribute("applicant", a, REQUEST);
			
			// Get the address validation entry - if there's none, assume we're validated
			GetAddressValidation avdao = new GetAddressValidation(con);
			AddressValidation av = avdao.get(a.getID());
			if (av == null) {
				ctx.release();
				result.setURL("/jsp/register/eMailValid.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Save in the request and set default result URL
			ctx.setAttribute("addr", av, REQUEST);
			result.setURL("/jsp/register/eMailValidate.jsp");
			
			// If we haven't provided a code, then just go to the JSP
			String code = ctx.getParameter("code");
			if (code == null) {
				ctx.release();
				result.setSuccess(true);
				return;
			}
			
			// Validate the e-mail address and code
			if (code.equalsIgnoreCase(av.getHash())) {
				SetAddressValidation avwdao = new SetAddressValidation(con);
				avwdao.delete(av.getID());
				result.setURL("/jsp/register/eMailValid.jsp");
			} else
				ctx.setAttribute("validationFailure", Boolean.FALSE, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setSuccess(true);
	}
}