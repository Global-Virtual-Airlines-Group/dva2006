// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.sql.Connection;
import java.util.Collection;
import java.util.stream.Collectors;

import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display Pilot progress through Flight Tours.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class TourProgressCommand extends AbstractViewCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs.
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		ViewContext<TourProgress> vc = initView(ctx, TourProgress.class);
		try {
			Connection con = ctx.getConnection();
			
			// Get the Tour
			GetTour tdao = new GetTour(con);
			Tour t = tdao.get(ctx.getID(), ctx.getDB());
			if ((ctx.getID() != 0) && (t == null))
				throw notFoundException("Invalid Flight Tour - " + ctx.getID());
			
			// Load the progress and the tour options
			tdao.setQueryStart(vc.getStart());
			tdao.setQueryMax(vc.getCount());
			vc.setResults(tdao.getProgress(ctx.getID()));
			ctx.setAttribute("tour", t, REQUEST);
			ctx.setAttribute("tours", tdao.getWithFlights(), REQUEST);
			
			// Load the Pilots
			Collection<Integer> IDs = vc.getResults().stream().map(TourProgress::getID).collect(Collectors.toSet());
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/tourProgress.jsp");
		result.setSuccess(true);
	}
}