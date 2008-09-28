// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.AddressValidation;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.AddressValidationHelper;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

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
				ctx.release();
				result.setURL("/jsp/pilot/eMailValid.jsp");
				result.setSuccess(true);
				return;
			}
				
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
			Mailer mailer = new Mailer(SystemData.getBoolean("smtp.testMode") ? addr : null);
			mctxt.addData("addrValid", av);
			mailer.setContext(mctxt);
			mailer.send(addr);
				
			// Set status attribute and log message
			ctx.setAttribute("resendEMail", Boolean.TRUE, REQUEST);
			ctx.setAttribute("addr", av, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setURL("/jsp/pilot/eMailValidate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}