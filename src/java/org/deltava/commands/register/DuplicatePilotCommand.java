//Copyright 2005, James Brickell & Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.register;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to send information about a duplicate pilot registration to HR.
 * @author James
 * @version 1.0
 * @since 1.0
 */

public class DuplicatePilotCommand extends AbstractCommand {
	
	/**
	 * Executes the Command.
	 * @param ctx Command Context.
	 * @throws CommandException if a command error occurs.
	 */
	
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get command result
		CommandResult result = ctx.getResult();
		result.setURL("jsp/register/duplicatePilotInfoSent.jsp");
		
		// Create e-mail message context
		MessageContext mctxt = new MessageContext();
		
		// Get e-mail message template
		try {
			Connection con = ctx.getConnection();
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("DUPLICATEREGISTRATION"));
			
			// Add data to message
			mctxt.addData("applicantmsg", ctx.getParameter("msgtohr"));
			
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Send the message to HR
		Mailer mailer = new Mailer(ctx.getUser());
		EMailAddress em = SystemData.get("airline.mail.hr");
		
		mailer.setContext(mctxt);
		mailer.send(em);
		
		
		
	}
}