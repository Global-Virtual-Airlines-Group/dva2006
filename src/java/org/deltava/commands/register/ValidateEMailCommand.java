// Copyright 2007, 2008, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.register;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.AddressValidation;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command for Applicants to validate their e-mail address.
 * @author Luke
 * @version 9.0
 * @since 2.0
 */

public class ValidateEMailCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the command result
		CommandResult result = ctx.getResult();
		try {
			Connection con = ctx.getConnection();
			
			// Get the applicant
			GetApplicant adao = new GetApplicant(con);
			Applicant a = adao.get(ctx.getID());
			if ((a == null) || (a.getStatus() != ApplicantStatus.PENDING)) {
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
				result.setSuccess(true);
				return;
			}
			
			// Validate the e-mail address and code
			if (code.trim().equalsIgnoreCase(av.getHash())) {
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

		result.setSuccess(true);
	}
}