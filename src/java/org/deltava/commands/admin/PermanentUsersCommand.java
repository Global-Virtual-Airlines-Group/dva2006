// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to list permanent users.
 * @author Luke
 * @version 6.4
 * @since 6.4
 */

public class PermanentUsersCommand extends AbstractViewCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		ViewContext vc = initView(ctx);
		try {
			GetPilotDirectory pdao = new GetPilotDirectory(ctx.getConnection());
			pdao.setQueryStart(vc.getStart());
			pdao.setQueryMax(vc.getCount());
			vc.setResults(pdao.getPermanent());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/permanentUsers.jsp");
		result.setSuccess(true);
	}
}