// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.sql.Connection;

import org.deltava.beans.system.MessageTemplate;

import org.deltava.commands.*;

import org.deltava.dao.GetMessageTemplate;
import org.deltava.dao.SetMessageTemplate;
import org.deltava.dao.DAOException;

import org.deltava.security.command.MessageAccessControl;

/**
 * A Web Site Command to delete Message Templates.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MessageTemplateDeleteCommand extends AbstractCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the message template
			GetMessageTemplate dao = new GetMessageTemplate(con);
			MessageTemplate mt = dao.get((String) ctx.getCmdParameter(Command.ID, null));
			if (mt == null)
				throw notFoundException("Invalid Message Template - " + ctx.getCmdParameter(Command.ID, null));

			// Check our access
			MessageAccessControl access = new MessageAccessControl(ctx, mt);
			access.validate();
			if (!access.getCanDelete())
				throw securityException("Cannot delete Message Template");

			// Get the write DAO and delete the template
			SetMessageTemplate wdao = new SetMessageTemplate(con);
			wdao.delete(mt.getName());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the Message Templates
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("msgtemplates", null, null);
		result.setSuccess(true);
	}
}