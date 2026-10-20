// Copyright 2005, 2006, 2016, 2020, 2022, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.register;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.ApplicantAccessControl;

/**
 * A Web Site Command to reject Applicants.
 * @author Luke
 * @version 13.0
 * @since 1.0
 */

public class ApplicantRejectCommand extends AbstractCommand {
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Init the message context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		Applicant a = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Applicant
			GetApplicant dao = new GetApplicant(con);
			a = dao.get(ctx.getID());
			if (a == null)
				throw notFoundException("Invalid Applicant", ctx.getID());

			// Check our access
			ApplicantAccessControl access = new ApplicantAccessControl(ctx, a);
			access.validate();
			if (!access.getCanReject())
				throw securityException("Cannot Reject Applicant");
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("APPREJECT"));
			mctxt.addData("applicant", a);
			
			// Create audit log entry
			AdminLogEntry le = new AdminLogEntry(a);
			le.setAuthorID(ctx.getUser().getID());
			le.setRemoteAddress(ctx.getRequest().getRemoteAddr(), ctx.getRequest().getRemoteHost());
			
			// Start the transaction
			ctx.startTX();
			
			// Remove the e-mail validation record
			GetAddressValidation avdao = new GetAddressValidation(con);
			AddressValidation addrValid = avdao.get(a.getID());
			if (addrValid != null) {
			   SetAddressValidation wavdao = new SetAddressValidation(con);
			   wavdao.delete(addrValid.getID());
			}
			
			// Write the audit log entry
			SetAuditLog adwdao = new SetAuditLog(con);
			adwdao.write(le);

			// Get the write DAO and reject the applicant
			SetApplicant wdao = new SetApplicant(con);
			wdao.reject(a);
			ctx.commitTX();

			// Save the applicant in the request
			ctx.setAttribute("applicant", a, REQUEST);
		} catch (DAOException de) {
		   ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Send e-mail notification
		if (a.getHasCAPTCHA()) {
			Mailer mailer = new Mailer(ctx.getUser());
			mailer.setContext(mctxt);
			mailer.send(a);
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/register/applicantReject.jsp");
		result.setSuccess(true);
	}
}