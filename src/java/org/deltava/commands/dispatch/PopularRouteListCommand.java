// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchRouteAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display popular route pairs.
 * @author Luke
 * @version 2.6
 * @since 2.2
 */

public class PopularRouteListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get parameters
		boolean noRoutes = Boolean.valueOf(ctx.getParameter("noRoutes")).booleanValue();
		boolean allFlights = Boolean.valueOf(ctx.getParameter("allFights")).booleanValue();
		int dayFilter = StringUtils.parse(ctx.getParameter("days"), 60);

		// Get the view context
		ViewContext vc = initView(ctx);
		try {
			GetFlightReportStatistics frdao = new GetFlightReportStatistics(ctx.getConnection());
			frdao.setQueryStart(vc.getStart());
			frdao.setQueryMax(vc.getCount());
			frdao.setDayFilter(dayFilter);
			vc.setResults(frdao.getPopularRoutes(noRoutes, allFlights));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Check for dispatch route creation access
		DispatchRouteAccessControl access = new DispatchRouteAccessControl(ctx, null);
		access.validate();
		ctx.setAttribute("access", access, REQUEST);
		
		// Save parsed day filter
		ctx.setAttribute("dayFilter", Integer.valueOf(dayFilter), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/dispatch/popularRoutes.jsp");
		result.setSuccess(true);
	}
}