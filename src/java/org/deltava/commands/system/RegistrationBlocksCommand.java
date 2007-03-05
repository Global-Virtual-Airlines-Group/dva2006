// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to view all Registration Block entries.
 * @author Luke
 * @version 1.0
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
		ViewContext vc = initView(ctx, 40);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO
			GetSystemData dao = new GetSystemData(con);
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