// Copyright 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import org.deltava.beans.help.ResponseTemplate;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.HelpDeskAccessControl;

/**
 * A Web Site Command to list all Help Desk Response Templates.
 * @author Luke
 * @version 7.0
 * @since 3.2
 */

public class ResponseTemplatesCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view start/end
		ViewContext<ResponseTemplate> vc = initView(ctx, ResponseTemplate.class);
		try {
			GetHelpTemplate dao = new GetHelpTemplate(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getAll());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save our access
		HelpDeskAccessControl access = new HelpDeskAccessControl(ctx, null);
		access.validate();
		ctx.setAttribute("access", access, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/help/rspTemplates.jsp");
		result.setSuccess(true);
	}
}