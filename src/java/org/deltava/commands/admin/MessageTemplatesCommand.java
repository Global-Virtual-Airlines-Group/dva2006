// Copyright 2005, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import org.deltava.beans.system.MessageTemplate;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.MessageAccessControl;

/**
 * A Web Site Command to display Message Template lists.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class MessageTemplatesCommand extends AbstractViewCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<MessageTemplate> vc = initView(ctx, MessageTemplate.class);
		try {
			GetMessageTemplate dao = new GetMessageTemplate(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getAll());
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