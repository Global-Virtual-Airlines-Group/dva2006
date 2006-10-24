// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.sql.Connection;

import org.deltava.beans.help.OnlineHelpEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.HelpAccessControl;

/**
 * A Web Site Command to update site Help entries.
 * @author Luke
 * @version 1.0
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
		
		try {
			Connection con = ctx.getConnection();
			
			// Create the entry from the request
			OnlineHelpEntry entry = new OnlineHelpEntry(ctx.getParameter("id"), ctx.getParameter("body"));
			entry.setSubject(ctx.getParameter("subject"));
			
			// Save the entry
			SetHelp wdao = new SetHelp(con);
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
			Connection con = ctx.getConnection();

			// Get the DAO and the help entry
			GetHelp dao = new GetHelp(con);
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
	 * @throws UnsupportedOperationException always
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		throw new UnsupportedOperationException();
	}
}