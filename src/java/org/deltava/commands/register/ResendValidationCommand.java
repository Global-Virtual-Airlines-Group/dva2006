// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.register;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.AddressValidation;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.AddressValidationHelper;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to resend an e-mail validation message to an Applicant.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ResendValidationCommand extends AbstractCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get command results
		CommandResult result = ctx.getResult();
		
		// Create the message context
		MessageContext mctxt = new MessageContext();
		try {
			Connection con = ctx.getConnection();
			
			// Get the Message Template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("APPEMAILUPDATE"));
			
			// Get the applicant
			GetApplicant adao = new GetApplicant(con);
			Applicant a = adao.get(ctx.getID());
			if ((a == null) || (a.getStatus() != Applicant.PENDING))
				throw notFoundException("Invalid Applicant - " + ctx.getID());
			
			// Get the e-mail validation entry
			GetAddressValidation avdao = new GetAddressValidation(con); 
			AddressValidation av = avdao.get(ctx.getID());
			if (av == null) {
				ctx.release();
				result.setURL("/jsp/register/eMailValid.jsp");
				result.setSuccess(true);
				return;
			}
			
			mctxt.addData("applicant", a);
			mctxt.addData("addrValid", av);
				
			// If we're providing a different e-mail address, use that one
			String email = ctx.getParameter("email");
			if (!StringUtils.isEmpty(email) && (!email.equals(av.getAddress()))) {
				a.setEmail(email);
				av.setAddress(email);
				av.setHash(AddressValidationHelper.calculateHashCode(email));
					
				// Start a transaction
				ctx.startTX();
					
				// Save the applicant
				SetApplicant awdao = new SetApplicant(con);
				awdao.write(a);
				
				// Save the address validation
				SetAddressValidation avwdao = new SetAddressValidation(con);
				avwdao.write(av);
				
				// Commit
				ctx.commitTX();
			}
				
			// Send the message
			Mailer mailer = new Mailer(null);
			mailer.setContext(mctxt);
			mailer.send(a);
				
			// Set status attributes and log message
			ctx.setAttribute("resendEMail", Boolean.TRUE, REQUEST);
			ctx.setAttribute("addr", av, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setURL("/jsp/register/eMailValidate.jsp");
		result.setSuccess(true);
	}
}