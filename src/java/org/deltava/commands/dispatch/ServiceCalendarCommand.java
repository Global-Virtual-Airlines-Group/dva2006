// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchScheduleAccessControl;

/**
 * A Web Site Command to display the ACARS Dispatch service calendar.
 * @author Luke
 * @version 2.4
 * @since 2.2
 */

public class ServiceCalendarCommand extends AbstractCalendarCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check if we load history
		boolean noHistory = Boolean.valueOf(ctx.getParameter("noHistory")).booleanValue();
		
		// Initialize the calendar context
		CalendarContext cctx = initCalendar(ctx);
		Collection<CalendarEntry> results = new ArrayList<CalendarEntry>();
		try {
			Connection con = ctx.getConnection();
			
			// Get the entries
			GetDispatchCalendar dcdao = new GetDispatchCalendar(con);
			Collection<DispatchScheduleEntry> entries = dcdao.getCalendar(cctx.getStartDate(), cctx.getDays(), ctx.getID());
			results.addAll(entries);
			
			// Save Access Rights
			Map<DispatchScheduleEntry, DispatchScheduleAccessControl> accessMap = new 
				HashMap<DispatchScheduleEntry, DispatchScheduleAccessControl>();
			
			// Get the Dispatcher IDs and calculate access
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<DispatchScheduleEntry> i = entries.iterator(); i.hasNext(); ) {
				DispatchScheduleEntry e = i.next();
				IDs.add(new Integer(e.getAuthorID()));
				
				// Get access
				DispatchScheduleAccessControl access = new DispatchScheduleAccessControl(ctx, e);
				access.validate();
				accessMap.put(e, access);
			}
			
			// Save the access controllers
			ctx.setAttribute("accessMap", accessMap, REQUEST);
			
			// Load the history if requested
			if (!noHistory) {
				Collection<ConnectionEntry> cons = dcdao.getDispatchConnections(cctx.getStartDate(), cctx.getDays());
				Collection<Integer> conIDs = new HashSet<Integer>();
				for (Iterator<ConnectionEntry> i = cons.iterator(); i.hasNext(); ) {
					DispatchConnectionEntry ce = (DispatchConnectionEntry) i.next();
					if (ce.getEndTime() == null) {
						i.remove();
						continue;
					}
						
					// Add the dispatcher ID
					conIDs.add(new Integer(ce.getPilotID()));
					
					// Load the flights
					Collection<FlightInfo> flights = dcdao.getDispatchedFlights(ce);
					for (FlightInfo fi : flights)
						ce.addFlight(fi);
					
					// Prune out any entries with no flights and less than 3 minutes long
					long conTime = (ce.getEndTime().getTime() - ce.getStartTime().getTime()) / 1000;
					if (flights.isEmpty() && (conTime < 150))
						i.remove();
				}
				
				// Save in the request
				results.addAll(cons);
				
				// Save the dispatcher IDs
				IDs.addAll(conIDs);
			}
			
			// Load the Dispatchers
			GetPilot pdao = new GetPilot(con);
			GetUserData uddao = new GetUserData(con);
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("pilots", pdao.get(udm), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save in the request
		ctx.setAttribute("entries", results, REQUEST);
		
		// Calculate our access to create new entries'
		DispatchScheduleAccessControl ac = new DispatchScheduleAccessControl(ctx, null);
		ac.validate();
		ctx.setAttribute("access", ac, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL((cctx.getDays() == 7) ? "/jsp/dispatch/calendarW.jsp" : "/jsp/dispatch/calendarM.jsp");
		result.setSuccess(true);
	}
}