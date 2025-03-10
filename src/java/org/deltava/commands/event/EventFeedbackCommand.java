// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.event.Event;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EventAccessControl;

/**
 * A Web Site Command to display Online Event user feedback. 
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class EventFeedbackCommand extends AbstractViewCommand {

	/**
	 * Executes the Command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<Feedback> vc = initView(ctx, Feedback.class);
		try {
			Connection con = ctx.getConnection();
			
			// Load the Event
			GetEvent dao = new GetEvent(con);
			Event e = dao.get(ctx.getID());
			if (e == null)
				throw notFoundException("Invalid Online Event - " + ctx.getID());
			
			// Check our access
			EventAccessControl ac = new EventAccessControl(ctx, e);
			ac.validate();
			if (!ac.getCanViewFeedback())
				throw securityException("Cannot view Online Event feedback");
			
			// Slice the feedback
			List<Feedback> results = new ArrayList<Feedback>(e.getFeedback());
			if (vc.getStart() <= results.size())
				vc.setResults(results.subList(vc.getStart(), Math.min(results.size(), vc.getStart() + vc.getCount())));	
			
			// Load Pilots
			GetPilot pdao = new GetPilot(con);
			Collection<Integer> IDs = e.getFeedback().stream().map(Feedback::getAuthorID).collect(Collectors.toSet());
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);

			// Calculate feedback and save in the request
			FeedbackScore sc = FeedbackScore.generate(e);
			ctx.setAttribute("event", e, REQUEST);
			ctx.setAttribute("score", sc, REQUEST);
			ctx.setAttribute("events", dao.getWithFeedback(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/event/eventFeedback.jsp");
		result.setSuccess(true);
	}
}