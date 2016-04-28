// Copyright 2005, 2006, 2007, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.event.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EventAccessControl;

/**
 * A Web Site Command to display the Online Event calendar.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class EventCalendarCommand extends AbstractCalendarCommand {
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Initialize the calendar context
		CalendarContext cctx = initCalendar(ctx);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the events
			GetEvent dao = new GetEvent(con);
			Collection<Event> events = dao.getEventCalendar(cctx.getRange());
			ctx.setAttribute("events", events, REQUEST);
			
			// Get future events
			ctx.setAttribute("futureEvents", dao.getFutureEvents(), REQUEST);
			
			// Get the Pilot IDs from the signups
			Collection<Integer> pilotIDs = new HashSet<Integer>();
			events.forEach(e -> e.getSignups().forEach(s -> pilotIDs.add(Integer.valueOf(s.getPilotID()))));
			
			// Load the signup user data
			GetUserData uddao = new GetUserData(con);
			UserDataMap udMap = uddao.get(pilotIDs);
			
			// Load the Pilots for the signups
			GetPilot pdao = new GetPilot(con);
			Map<Integer, Pilot> pilots = pdao.get(udMap);
			ctx.setAttribute("pilots", pilots, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Calculate our access to create new events
		EventAccessControl access = new EventAccessControl(ctx, null);
		access.validate();
		ctx.setAttribute("access", access, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL((cctx.getDays() == 7) ? "/jsp/event/calendarW.jsp" : "/jsp/event/calendarM.jsp");
		result.setSuccess(true);
	}
}