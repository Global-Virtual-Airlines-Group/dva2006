// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.stats.Tour;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.TourAccessControl;

/**
 * A Web Site command to display Flight Tour user feedback results.
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class TourFeedbackCommand extends AbstractViewCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		ViewContext<Feedback> vc = initView(ctx, Feedback.class);
		try {
			Connection con = ctx.getConnection();
			
			// Load the Tour
			GetTour dao = new GetTour(con);
			Tour t = dao.get(ctx.getID(), ctx.getDB());
			if (t == null)
				throw notFoundException("Invalid Flight Tour - " + ctx.getID());
			
			// Check our access
			TourAccessControl ac = new TourAccessControl(ctx, t);
			ac.validate();
			if (!ac.getCanViewFeedback())
				throw securityException("Cannot view Flight Tour feedback");
			
			// Slice the feedback
			List<Feedback> results = new ArrayList<Feedback>(t.getFeedback());
			if (vc.getStart() <= results.size())
				vc.setResults(results.subList(vc.getStart(), Math.min(results.size(), vc.getStart() + vc.getCount())));	
			
			// Load Pilots
			GetPilot pdao = new GetPilot(con);
			Collection<Integer> IDs = t.getFeedback().stream().map(Feedback::getAuthorID).collect(Collectors.toSet());
			ctx.setAttribute("authors", pdao.getByID(IDs, "PILOTS"), REQUEST);
			
			// Calculate feedback and save in the request
			FeedbackScore sc = FeedbackScore.generate(t);
			ctx.setAttribute("tour", t, REQUEST);
			ctx.setAttribute("score", sc, REQUEST);
			ctx.setAttribute("tours", dao.getWithFeedback(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/tourFeedback.jsp");
		result.setSuccess(true);
	}
}