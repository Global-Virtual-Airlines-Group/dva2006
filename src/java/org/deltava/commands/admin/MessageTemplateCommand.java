// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.sql.Connection;

import org.deltava.beans.system.MessageTemplate;

import org.deltava.commands.*;

import org.deltava.dao.GetMessageTemplate;
import org.deltava.dao.DAOException;

import org.deltava.security.command.MessageAccessControl;

/**
 * A Web Site Command to edit Message Templates.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MessageTemplateCommand extends AbstractCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check if we're creating a new template
		boolean isNew = "new".equals(ctx.getCmdParameter(Command.OPERATION, null));
		
		// Check our access
		MessageAccessControl access = new MessageAccessControl(ctx, null);
		access.validate();
		boolean isOK = isNew ? access.getCanCreate() : access.getCanEdit();
		if (!isOK)
			throw securityException("Cannot create/edit Message Template");
		
		// Get the command results
		CommandResult result = ctx.getResult();
		
		// If we're creating a new template, redirect to the JSP
		if (isNew) {
			ctx.setAttribute("access", access, REQUEST);
			result.setURL("/jsp/admin/msgTemplate.jsp");
			result.setSuccess(true);
			return;
		}

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the message
			GetMessageTemplate dao = new GetMessageTemplate(con);
			MessageTemplate msg = dao.get((String) ctx.getCmdParameter(Command.ID, null));
			if (msg == null)
				throw notFoundException("Invalid Message Template - " + ctx.getCmdParameter(Command.ID, null));
			
			// Update our access to calculate deletion rights
			access = new MessageAccessControl(ctx, msg);
			access.validate();
			
			// Save the template in the request
			ctx.setAttribute("access", access, REQUEST);
			ctx.setAttribute("template", msg, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setURL("/jsp/admin/msgTemplate.jsp");
		result.setSuccess(true);
	}
}