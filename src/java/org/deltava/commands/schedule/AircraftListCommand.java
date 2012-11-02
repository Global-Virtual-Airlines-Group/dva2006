// Copyright 2006, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display aircraft data.
 * @author Luke
 * @version 5.0
 * @since 1.0
 */

public class AircraftListCommand extends AbstractViewCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		ViewContext vc = initView(ctx);
		try {
			GetAircraft dao = new GetAircraft(ctx.getConnection());
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
		result.setURL("/jsp/schedule/aircraftList.jsp");
		result.setSuccess(true);
	}
}