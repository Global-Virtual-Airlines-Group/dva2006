// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.sql.Connection;
import java.util.Collection;

import org.deltava.beans.AuditLog;
import org.deltava.beans.system.Prompt;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.BeanUtils;

/**
 * A Web Site Command to edit LLM prompts. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class PromptCommand extends AbstractAuditFormCommand {

	/**
	 * Callback method called when saving the prompt.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Build the prompot
		String id = ctx.getParameter("id");
		Prompt p = new Prompt(id);
		p.setUser(ctx.getParameter("user"));
		p.setSystem(ctx.getParameter("system"));
		try {
			Connection con = ctx.getConnection();
			
			// Get the existing prompt
			GetSystemData dao = new GetSystemData(con);
			Prompt op = dao.getPrompt(id);
			
			// Build the audit log entry
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(op, p);
			AuditLog ae = AuditLog.create(p, delta, ctx.getUser().getID());

			// Start transaction
			ctx.startTX();

			// Save the prompt
			SetSystemData wdao = new SetSystemData(con);
			wdao.write(p);
			
			// Write the audit log
			writeAuditLog(ctx, ae);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/promptUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the prompt.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		String id = ctx.getParameter("id");
		try {
			Connection con = ctx.getConnection();
			GetSystemData dao = new GetSystemData(con);
			Prompt p = dao.getPrompt(id);
			if (p == null)
				throw notFoundException("Invalid Prompt ID", id);
			
			// Load audit history and save in reuqest
			readAuditLog(ctx, p);
			ctx.setAttribute("prompt", p, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/promptEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the prompt. <i>NOT IMPLEMENTED</i>
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}