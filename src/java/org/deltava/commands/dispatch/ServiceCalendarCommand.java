// Copyright 2008, 2009, 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.util.*;
import java.time.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;

import org.deltava.commands.*;
import org.deltava.comparators.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchScheduleAccessControl;

/**
 * A Web Site Command to display the ACARS Dispatch service calendar.
 * @author Luke
 * @version 7.0
 * @since 2.2
 */

public class ServiceCalendarCommand extends AbstractCalendarCommand {
	
	private class ConnectionEntryComparator implements Comparator<DispatchConnectionEntry> {
		
		ConnectionEntryComparator() {
			super();
		}
		
		@Override
		public int compare(DispatchConnectionEntry dce1, DispatchConnectionEntry dce2) {
			int tmpResult = Integer.valueOf(dce1.getAuthorID()).compareTo(Integer.valueOf(dce2.getAuthorID()));
			if (tmpResult == 0)
				tmpResult = dce1.getStartTime().compareTo(dce2.getStartTime());
			
			return tmpResult;
		}
	}
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check if we load history
		boolean noHistory = Boolean.valueOf(ctx.getParameter("noHistory")).booleanValue();
		long now = System.currentTimeMillis();
		
		// Initialize the calendar context
		CalendarContext cctx = initCalendar(ctx);
		List<CalendarEntry> entries = new ArrayList<CalendarEntry>();
		List<DispatchConnectionEntry> conEntries = new ArrayList<DispatchConnectionEntry>();
		try {
			Connection con = ctx.getConnection();
			
			// Get the entries
			GetDispatchCalendar dcdao = new GetDispatchCalendar(con);
			Collection<DispatchScheduleEntry> schedEntries = dcdao.getCalendar(ctx.getID(), cctx.getRange());
			for (Iterator<DispatchScheduleEntry> i = schedEntries.iterator(); i.hasNext(); ) {
				DispatchScheduleEntry se = i.next();
				if (se.getEndTime().toEpochMilli() >= now)
					entries.add(se);		
				else
					i.remove();
			}
			
			// Save Access Rights
			Map<DispatchScheduleEntry, DispatchScheduleAccessControl> accessMap = new 
				HashMap<DispatchScheduleEntry, DispatchScheduleAccessControl>();
			
			// Get the Dispatcher IDs and calculate access
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<DispatchScheduleEntry> i = schedEntries.iterator(); i.hasNext(); ) {
				DispatchScheduleEntry e = i.next();
				IDs.add(Integer.valueOf(e.getAuthorID()));
				
				// Get access
				DispatchScheduleAccessControl access = new DispatchScheduleAccessControl(ctx, e);
				access.validate();
				accessMap.put(e, access);
			}
			
			// Save the access controllers
			ctx.setAttribute("accessMap", accessMap, REQUEST);
			
			// Load the history if requested
			if (!noHistory) {
				Collection<ConnectionEntry> cons = dcdao.getDispatchConnections(cctx.getRange());
				Collection<Integer> conIDs = new HashSet<Integer>();
				for (Iterator<ConnectionEntry> i = cons.iterator(); i.hasNext(); ) {
					DispatchConnectionEntry ce = (DispatchConnectionEntry) i.next();
					conIDs.add(Integer.valueOf(ce.getPilotID()));
					
					// Load the flights
					Collection<FlightInfo> flights = dcdao.getDispatchedFlights(ce);
					ce.addFlights(flights);
					
					// Prune out any entries with no flights and less than 2 minutes long
					if (ce.getEndTime() != null) {
						Duration conTime = Duration.between(ce.getStartTime(), ce.getEndTime());
						if (!flights.isEmpty() || (conTime.getSeconds() > 120))
							conEntries.add(ce);
					} else
						conEntries.add(ce);
				}
				
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
		
		// Combine service entries if they aren't very separated
		Collections.sort(conEntries, new ConnectionEntryComparator());
		DispatchConnectionEntry lastEntry = null;
		
		for (Iterator<DispatchConnectionEntry> i = conEntries.iterator(); i.hasNext(); ) {
			DispatchConnectionEntry ce = i.next();
			if ((lastEntry != null) && ((ce.getAuthorID() != lastEntry.getAuthorID()) || (lastEntry.getEndTime() == null)))
				lastEntry = null;
			
			if (lastEntry == null) {
				lastEntry = ce;
				entries.add(ce);
			} else {
				Instant endTime = (ce.getEndTime() == null) ? Instant.now() : ce.getEndTime();
				long timeDiff = Duration.between(ce.getStartTime(), endTime).getSeconds();
				if (timeDiff < 900) {
					lastEntry.setEndTime(ce.getEndTime());
					lastEntry.addFlights(ce.getFlights());
				} else {
					lastEntry = ce;
					entries.add(ce);	
				}
			}
		}
		
		// Save in the request
		Collections.sort(entries, new CalendarEntryComparator());
		ctx.setAttribute("entries", entries, REQUEST);
		
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