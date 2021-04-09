// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import org.deltava.beans.econ.EliteLevel;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display Elite Leve definitions 
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class EliteLevelListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<EliteLevel> vctx = initView(ctx, EliteLevel.class);
		try {
			GetElite dao = new GetElite(ctx.getConnection());
			dao.setQueryStart(vctx.getStart());
			dao.setQueryMax(vctx.getCount());
			vctx.setResults(dao.getLevels());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/econ/eliteLevels.jsp");
		result.setSuccess(true);
	}
}