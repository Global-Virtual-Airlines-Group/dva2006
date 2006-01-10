// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.text.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.event.*;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to display the Online Event calendar.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EventCalendarCommand extends AbstractCommand {
	
	private static final DateFormat _df = new SimpleDateFormat("MM/dd/yyyy");
	private static final List TYPE_OPTIONS = ComboUtils.fromArray(new String[] {"Month", "Week"}, new String[] { "31", "7"});

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the number of days and start date
		int days = Integer.parseInt((String) ctx.getCmdParameter(OPERATION, "7"));
		Date startDate = null;
		try {
			startDate = _df.parse(ctx.getParameter("startDate"));
		} catch (Exception e) {
			Calendar cld = Calendar.getInstance();
			cld.set(Calendar.HOUR, 0);
			cld.set(Calendar.MINUTE, 0);
			cld.set(Calendar.SECOND, 0);
			startDate = cld.getTime();
		}
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the events
			GetEvent dao = new GetEvent(con);
			Collection<Event> events = dao.getEventCalendar(startDate, days);
			ctx.setAttribute("events", events, REQUEST);
			
			// Get the Pilot IDs from the signups
			Set<Integer> pilotIDs = new HashSet<Integer>();
			for (Iterator<Event> i = events.iterator(); i.hasNext(); ) {
				Event e = i.next();
				for (Iterator<Signup> si = e.getSignups().iterator(); si.hasNext(); ) {
					Signup s = si.next();
					pilotIDs.add(new Integer(s.getPilotID()));
				}
			}
			
			// Load the signup user data
			GetUserData uddao = new GetUserData(con);
			UserDataMap udMap = uddao.get(pilotIDs);
			
			// Load the Pilots for the signups
			Map<Integer, Pilot> pilots = new HashMap<Integer, Pilot>();
			GetPilot pdao = new GetPilot(con);
			for (Iterator<String> i = udMap.getTableNames().iterator(); i.hasNext(); ) {
				String dbTableName = i.next();
				if (UserDataMap.isPilotTable(dbTableName))
					pilots.putAll(pdao.getByID(udMap.getByTable(dbTableName), dbTableName));
			}
			
			// Save the pilots
			ctx.setAttribute("pilots", pilots, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save the calendar options
		ctx.setAttribute("startDate", startDate, REQUEST);
		ctx.setAttribute("typeOptions", TYPE_OPTIONS, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL((days == 7) ? "/jsp/event/calendarW.jsp" : "/jsp/event/calendarM.jsp");
		result.setSuccess(true);
	}
}