//Copyright 2005, 2006, 2016, 2018, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.register;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to send information about a duplicate pilot registration to HR.
 * @author James
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class DuplicatePilotCommand extends AbstractCommand {
	
	/**
	 * Executes the Command.
	 * @param ctx Command Context.
	 * @throws CommandException if a command error occurs.
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the origin address
		String fromAddr = ctx.getParameter("email");
		if (StringUtils.isEmpty(fromAddr))
			throw securityException("No Address");
		
		// Create e-mail message context
		MessageContext mctxt = new MessageContext();
		try {
			GetMessageTemplate mtdao = new GetMessageTemplate(ctx.getConnection());
			mctxt.setTemplate(mtdao.get("DUPEREGISTER"));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Add data to message
		mctxt.addData("firstName", ctx.getParameter("firstName"));
		mctxt.addData("lastName", ctx.getParameter("lastName"));
		mctxt.addData("email", ctx.getParameter("email"));
		mctxt.addData("msgText", ctx.getParameter("msgText"));
		mctxt.addData("airlineName", ctx.getParameter("airline"));
		
		// Send the message to HR
		Mailer mailer = new Mailer(MailUtils.makeAddress(fromAddr));
		mailer.setContext(mctxt);
		mailer.send(MailUtils.makeAddress(SystemData.get("airline.mail.hr")));
	
		// Get command result
		CommandResult result = ctx.getResult();
		result.setURL("jsp/register/dupePilotInfoSent.jsp");
		result.setSuccess(true);
	}
}