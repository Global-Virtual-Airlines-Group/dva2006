// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.register;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.AddressValidation;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.Authenticator;
import org.deltava.security.SQLAuthenticator;

import org.deltava.util.PasswordGenerator;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to resent the Applicant welcome message.
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
				throw notFoundException("Invalid Applicant - " + ctx.getID());
			
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
			} else if ((a.getStatus() == Applicant.PENDING) && (addrValid != null)) {
				mctxt.setTemplate(mtdao.get("APPEMAILUPDATE"));
				mctxt.addData("applicant", a);
				mctxt.addData("addrValid", addrValid);
			} else if (a.getStatus() == Applicant.APPROVED) {
				mctxt.addData("applicant", a);
				mctxt.setTemplate(mtdao.get("APPAPPROVE"));
				
				// Get Pilot profile
				GetPilot pdao = new GetPilot(con);
				Pilot usr = pdao.get(a.getPilotID());
				if (usr == null)
					throw notFoundException("Cannot load Pilot Profile");
				
				// Load the equipment profile
				GetEquipmentType eqdao = new GetEquipmentType(con);
				EquipmentType eqType = eqdao.get(a.getEquipmentType());
				if (eqType == null)
					throw notFoundException("Cannot load Equipment Profle");
				
				// Add the equipment profile
				mctxt.addData("eqType", eqType);
				ctx.setAttribute("passwordUpdated", Boolean.TRUE, REQUEST);
				
				// Update the password
				a.setPassword(PasswordGenerator.generate(8));
				usr.setPassword(a.getPassword());
				
				// Update the user
				Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
				if (auth instanceof SQLAuthenticator) {
					SQLAuthenticator sqlAuth = (SQLAuthenticator) auth;
					sqlAuth.setConnection(con);
					sqlAuth.updatePassword(usr, usr.getPassword());
					sqlAuth.clearConnection();
				} else
					auth.updatePassword(usr, usr.getPassword());
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