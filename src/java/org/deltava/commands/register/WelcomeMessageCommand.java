// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.register;

import java.sql.Connection;

import org.deltava.beans.Applicant;
import org.deltava.beans.system.AddressValidation;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.ApplicantAccessControl;

/**
 * A Web Site Command to resent the applicant welcome message.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class WelcomeMessageCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Create the message context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());
		
		Applicant a = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the Applicant
			GetApplicant adao = new GetApplicant(con);
			a = adao.get(ctx.getID());
			if (a == null)
				throw new CommandException("Invalid Applicant - " + ctx.getID());
			
			// Check our access
			ApplicantAccessControl access = new ApplicantAccessControl(ctx, a);
			access.validate();
			if ((!access.getCanApprove()) && (!access.getCanReject()))
				throw securityException("Cannot approve/reject Applicant");
			
			// Get the Questionnaire
			GetQuestionnaire qdao = new GetQuestionnaire(con);
			Examination ex = qdao.getByApplicantID(ctx.getID());
			
			// Get the Address Validation object
			GetAddressValidation avdao = new GetAddressValidation(con);
			AddressValidation addrValid = avdao.get(ctx.getID());
			
			// Get the Message template - if the questionaire has been submitted or scored, then just send e-mail validation
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			if ((ex != null) && (ex.getStatus() == Test.NEW)) {
			   mctxt.setTemplate(mtdao.get("USERREGISTER"));
			   mctxt.addData("questionnaire", ex);
			   mctxt.addData("applicant", a);
			} else {
			   mctxt.setTemplate(mtdao.get("ADDRVALIDATE"));
			   mctxt.addData("person", a);
			}
			
			// Add data to the message
			mctxt.addData("addr", addrValid);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save applicant in the request
		ctx.setAttribute("applicant", a, REQUEST);
		
		// Send an e-mail notification to the user
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctxt);
		mailer.send(a);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/register/welcomeResent.jsp");
		result.setSuccess(true);
	}
}