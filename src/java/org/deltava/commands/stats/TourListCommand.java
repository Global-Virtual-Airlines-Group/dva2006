// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;

import org.deltava.beans.stats.Tour;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.TourAccessControl;

/**
 * A Web Site Command to list flight Tours.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class TourListCommand extends AbstractViewCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs.
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<Tour> vc = initView(ctx, Tour.class);
		try {
			GetTour dao = new GetTour(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getAll());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save access for all Tours
		Map<Tour, TourAccessControl> acMap = new HashMap<Tour, TourAccessControl>();
		for (Iterator<Tour> i = vc.getResults().iterator(); i.hasNext(); ) {
			Tour t = i.next();
			TourAccessControl ac = new TourAccessControl(ctx, t);
			ac.validate();
			if (ac.getCanRead())
				acMap.put(t, ac);
			else
				i.remove();
		}
		
		// Save access control
		TourAccessControl ac = new TourAccessControl(ctx, null);
		ac.validate();
		ctx.setAttribute("access", ac, REQUEST);
		ctx.setAttribute("accessMap", acMap, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/tourList.jsp");
		result.setSuccess(true);
	}
}