// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the available ACARS multi-player liveries. 
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class LiveryListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the view context
		ViewContext vc = initView(ctx);
		try {
			GetACARSLivery dao = new GetACARSLivery(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			
			// Load the liveries
			vc.setResults(dao.get(SystemData.getAirline(ctx.getParameter("airline"))));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save airlines
		ctx.setAttribute("airlines", SystemData.getAirlines().values(), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/acars/liveryList.jsp");
		result.setSuccess(true);
	}
}