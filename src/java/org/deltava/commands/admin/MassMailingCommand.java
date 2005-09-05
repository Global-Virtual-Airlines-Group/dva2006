// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FileUpload;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

/**
 * A Web Site Command to send group e-mail messages.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MassMailingCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result and equipment type to process
		CommandResult result = ctx.getResult();
		String eqType = ctx.getParameter("eqType");

		// If we're just executing the command and not in the HR role, get our equipment type and return
		if ((eqType == null) && (!ctx.getRequest().isUserInRole("HR"))) {
			ctx.setAttribute("eqTypes", ctx.getUser().getEquipmentType(), REQUEST);
			result.setURL("/jsp/admin/massMail.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Initialize the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());
		mctxt.addData("body", ctx.getParameter("body"));

		List pilots = null;
		try {
			Connection con = ctx.getConnection();

			// Get a list of equipment types
			GetEquipmentType eqdao = new GetEquipmentType(con);
			ctx.setAttribute("eqTypes", eqdao.getActive(), REQUEST);
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("MASSMAIL"));
			
			// Check if we're sending to a different equipment type
			if ((!ctx.getRequest().isUserInRole("HR")) && (!eqType.equals(ctx.getUser().getEquipmentType())))
				throw securityException("Equipment Type " + eqType + " != "
						+ ctx.getUser().getEquipmentType());

			// If we're posting to the command, get the pilots to display
			if (eqType != null) {
				GetPilot dao = new GetPilot(con);
				pilots = dao.getPilotsByEQ(ctx.getParameter("eqType"));
				ctx.setAttribute("eqType", eqType, REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// If we're not sending to anyone, just redirect to the JSP
		if (eqType == null) {
			result.setURL("/jsp/admin/massMail.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Create the e-mail message
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctxt);
		
		// Add an attachment if we have one
		FileUpload file = ctx.getFile("fAttach");
		if (file != null)
			mailer.setAttachment(new MemoryDataSource(file.getName(), file.getBuffer()));
		
		// Send the message
		mailer.send(pilots);
		
		// Save results
		ctx.setAttribute("msgSent", new Integer(pilots.size()), REQUEST);
		
		// Forward to the JSP
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/admin/massMailComplete.jsp");
		result.setSuccess(true);
	}
}