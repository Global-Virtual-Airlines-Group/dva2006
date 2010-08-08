// Copyright 2006, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import org.deltava.beans.help.OnlineHelpEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.HelpAccessControl;

/**
 * A Web Site Command to update site Help entries.
 * @author Luke
 * @version 3.2
 * @since 1.0
 */

public class HelpEntryCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving a Help Entry.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Check our Access
		HelpAccessControl access = new HelpAccessControl(ctx);
		access.validate();
		if (!access.getCanEdit())
			throw securityException("Cannot edit Online Help Entry");
		
		// Create the entry from the request
		OnlineHelpEntry entry = new OnlineHelpEntry(ctx.getParameter("id"), ctx.getParameter("body"));
		entry.setSubject(ctx.getParameter("subject"));

		// Save the entry
		try {
			SetHelp wdao = new SetHelp(ctx.getConnection());
			wdao.write(entry);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to Help list
		CommandResult result = ctx.getResult(); 
		result.setURL("helplist.do", null, null);
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing a Help Entry.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {
		try {
			GetOnlineHelp dao = new GetOnlineHelp(ctx.getConnection());
			ctx.setAttribute("help", dao.get(ctx.getParameter("id")), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Check our Access
		HelpAccessControl access = new HelpAccessControl(ctx);
		access.validate();

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL(access.getCanEdit() ? "/jsp/admin/onlineHelpEdit.jsp" : "/jsp/admin/onlineHelpView.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading a Help Entry. <i>NOT IMPLEMENTED</i>
	 * @param ctx the Command context
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}