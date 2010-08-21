// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.sql.Connection;

import org.deltava.beans.help.ResponseTemplate;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.HelpDeskAccessControl;

/**
 * A Web Site Command to delete a Help Desk response template.
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class ResponseTemplateDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check our access
		HelpDeskAccessControl ac = new HelpDeskAccessControl(ctx, null);
		ac.validate();
		if (!ac.getCanUpdateTemplate())
			throw securityException("Cannot delete Help Desk response template");
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the template
			GetHelpTemplate dao = new GetHelpTemplate(con);
			ResponseTemplate rsp = dao.get(ctx.getParameter("id"));
			if (rsp == null)
				throw notFoundException("Cannot find response template - " + ctx.getParameter("id"));
			
			// Delete the template
			SetHelp wdao = new SetHelp(con);
			wdao.deleteTemplate(rsp.getTitle());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the view
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("rsptemplates.do");
		result.setSuccess(true);
	}
}