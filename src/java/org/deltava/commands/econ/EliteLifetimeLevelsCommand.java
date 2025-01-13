// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import org.deltava.beans.econ.EliteLifetime;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display lifetime Elite status levels.
 * @author Luke
 * @version 11.5
 * @since 11.5
 */

public class EliteLifetimeLevelsCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<EliteLifetime> vctx = initView(ctx, EliteLifetime.class);
		try {
			GetElite dao = new GetElite(ctx.getConnection());
			dao.setQueryStart(vctx.getStart());
			dao.setQueryMax(vctx.getCount());
			vctx.setResults(dao.getLifetimeLevels());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/econ/eliteLTLevels.jsp");
		result.setSuccess(true);
	}
}