// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;

import org.deltava.beans.schedule.OceanicRoute;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display the North Atlantic Track plotting map.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NATPlotCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		Collection<Date> dates = null;
		try {
			GetRoute dao = new GetRoute(ctx.getConnection());
			dao.setQueryMax(31);
			dates = dao.getOceanicTrackDates(OceanicRoute.NAT);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Convert the dates
		Collection<String> fmtDates = new LinkedHashSet<String>();
		for (Iterator<Date> i = dates.iterator(); i.hasNext(); ) {
			Date dt = i.next();
			fmtDates.add(StringUtils.format(dt, ctx.getUser().getDateFormat()));
		}
		
		// Save the dates in the request
		ctx.setAttribute("dates", fmtDates, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/natPlot.jsp");
		result.setSuccess(true);
	}
}