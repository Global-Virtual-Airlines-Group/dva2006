// Copyright 2008, 2009, 2016, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import org.deltava.beans.schedule.ScheduleRoute;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchRouteAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display popular route pairs.
 * @author Luke
 * @version 10.2
 * @since 2.2
 */

public class PopularRouteListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get parameters
		boolean noRoutes = Boolean.parseBoolean(ctx.getParameter("noRoutes"));
		boolean allFlights = Boolean.parseBoolean(ctx.getParameter("allFights"));
		int dayFilter = StringUtils.parse(ctx.getParameter("days"), 60);

		// Get the view context
		ViewContext<ScheduleRoute> vc = initView(ctx, ScheduleRoute.class);
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