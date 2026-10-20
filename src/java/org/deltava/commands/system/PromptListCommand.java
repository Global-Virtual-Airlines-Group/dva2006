// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import org.deltava.beans.system.Prompt;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display LLM prompts.
 * @author Luke
 * @version 13.0
 * @since 12.5
 */

public class PromptListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<Prompt> vctx = initView(ctx, Prompt.class);
		try {
			GetSystemData dao = new GetSystemData(ctx.getConnection());
			dao.setQueryStart(vctx.getStart());
			dao.setQueryMax(vctx.getCount());
			vctx.setResults(dao.getPrompts());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/promptList.jsp");
		result.setSuccess(true);
	}
}