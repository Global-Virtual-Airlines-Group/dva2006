// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.admin;

import java.sql.Connection;

import org.deltava.beans.system.MessageTemplate;

import org.deltava.commands.*;

import org.deltava.dao.GetMessageTemplate;
import org.deltava.dao.SetMessageTemplate;
import org.deltava.dao.DAOException;

import org.deltava.security.command.MessageAccessControl;

/**
 * A Web Site Command to save Message Templates. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MessageTemplateSaveCommand extends AbstractCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

		// Check if we're creating a new template
		boolean isNew = (ctx.getCmdParameter(Command.ID, null) == null);
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the existing template
			MessageTemplate mt = null;
			if (!isNew) {
				GetMessageTemplate dao = new GetMessageTemplate(con);
				mt = dao.get((String) ctx.getCmdParameter(Command.ID, null));
				if (mt == null)
					throw new CommandException("Invalid Message Template - " + ctx.getCmdParameter(Command.ID, null));
				
				// Check our access
				MessageAccessControl access = new MessageAccessControl(ctx, mt);
				access.validate();
				if (!access.getCanEdit())
					throw securityException("Cannot edit Message Template");
			} else {
				// Check our access
				MessageAccessControl access = new MessageAccessControl(ctx, mt);
				access.validate();
				if (!access.getCanCreate())
					throw securityException("Cannot create Message Template");
				
				mt = new MessageTemplate(ctx.getParameter("name"));
			}
			
			// Load from the request
			mt.setSubject(ctx.getParameter("subject"));
			mt.setDescription(ctx.getParameter("desc"));
			mt.setBody(ctx.getParameter("body"));
			
			// Get the write DAO and update the template
			SetMessageTemplate wdao = new SetMessageTemplate(con);
			wdao.write(mt);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the Message Templates
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setURL("msgtemplates", null, null);
		result.setSuccess(true);
	}
}