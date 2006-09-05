// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.sql.Connection;

import org.deltava.beans.EMailAddress;
import org.deltava.beans.system.AddressValidation;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.AddressValidationHelper;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to resend an e-mail validation message.
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
		
		// Create the message context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());
		
		// Get command results
		CommandResult result = ctx.getResult();

		try {
			Connection con = ctx.getConnection();
			
			// Get the Message Template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("EMAILUPDATE"));
			
			// Get the e-mail validation entry
			GetAddressValidation avdao = new GetAddressValidation(con); 
			AddressValidation av = avdao.get(ctx.getUser().getID());
			if (av == null) {
				ctx.setAttribute("pilot", ctx.getUser(), REQUEST);
				result.setURL("/jsp/register/eMailValid.jsp");
			} else {
				mctxt.addData("addrValid", av);
				result.setURL("/jsp/register/eMailValidate.jsp");
				
				// If we're providing a different e-mail address, use that one
				String email = ctx.getParameter("email");
				if (!StringUtils.isEmpty(email) && (!email.equals(av.getAddress()))) {
					av.setAddress(email);
					av.setHash(AddressValidationHelper.calculateHashCode(email));
					SetAddressValidation avwdao = new SetAddressValidation(con);
					avwdao.write(av);
				}
				
				// Create the address
				EMailAddress addr = Mailer.makeAddress(av.getAddress(), ctx.getUser().getName()); 
				
				// Send the message
				Mailer mailer = new Mailer(null);
				mailer.setContext(mctxt);
				mailer.send(addr);
				
				// Set status attribute and log message
				ctx.setAttribute("resendEMail", Boolean.TRUE, REQUEST);
				ctx.setAttribute("emailAddr", addr, REQUEST);
				ctx.setAttribute("addr", av, REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setSuccess(true);
	}
}