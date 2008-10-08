// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to view all Registration Block entries.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class RegistrationBlocksCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the start/view/count
		ViewContext vc = initView(ctx);
		try {
			GetSystemData dao = new GetSystemData(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getBlocks());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/regBlocks.jsp");
		result.setSuccess(true);
	}
}