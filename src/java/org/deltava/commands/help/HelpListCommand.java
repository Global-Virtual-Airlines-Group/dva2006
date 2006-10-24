// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display Online Help Entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class HelpListCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Init the view context
		ViewContext vc = initView(ctx);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the help entries
			GetHelp dao = new GetHelp(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getAll());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/onlineHelpList.jsp");
		result.setSuccess(true);
	}
}