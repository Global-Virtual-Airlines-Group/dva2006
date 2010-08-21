// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import org.deltava.beans.help.ResponseTemplate;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.HelpDeskAccessControl;

/**
 * A Web Site Command to handle Help Desk Response Templates. 
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class ResponseTemplateCommand extends AbstractFormCommand {

	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {

		// Check our access
		HelpDeskAccessControl ac = new HelpDeskAccessControl(ctx, null);
		ac.validate();
		if (!ac.getCanUpdateTemplate())
			throw securityException("Cannot update Help Desk response template");
		
		// Create the bean
		ResponseTemplate tmp = new ResponseTemplate();
		tmp.setTitle(ctx.getParameter("title"));
		tmp.setBody(ctx.getParameter("body"));

		try {
			SetHelp wdao = new SetHelp(ctx.getConnection());
			wdao.write(tmp);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the view
		CommandResult result = ctx.getResult();
		result.setURL("rsptemplates.do");
		result.setSuccess(true);
	}

	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		// Check our access
		HelpDeskAccessControl ac = new HelpDeskAccessControl(ctx, null);
		ac.validate();
		if (!ac.getCanUpdateTemplate())
			throw securityException("Cannot update Help Desk response template");
		
		// Save access
		ctx.setAttribute("access", ac, REQUEST);
		
		try {
			GetHelpTemplate dao = new GetHelpTemplate(ctx.getConnection());
			ResponseTemplate rsp = dao.get(ctx.getParameter("id"));
			ctx.setAttribute("template", rsp, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/help/rspTemplate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when reading the form. Calls execEdit.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}