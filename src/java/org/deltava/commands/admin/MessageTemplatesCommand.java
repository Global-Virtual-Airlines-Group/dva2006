// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.admin;

import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetMessageTemplate;
import org.deltava.dao.DAOException;

import org.deltava.security.command.MessageAccessControl;

/**
 * A Web Site Command to display Message Template lists.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MessageTemplatesCommand extends AbstractCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the templates
			GetMessageTemplate dao = new GetMessageTemplate(con);
			ctx.setAttribute("templates", dao.getAll(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save our access level
		MessageAccessControl access = new MessageAccessControl(ctx, null);
		access.validate();
		ctx.setAttribute("access", access, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/msgTemplateList.jsp");
		result.setSuccess(true);
	}
}