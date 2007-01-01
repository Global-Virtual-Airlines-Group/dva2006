// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.DateTime;
import org.deltava.beans.TZInfo;

import org.deltava.beans.event.Event;
import org.deltava.beans.schedule.*;
import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EventAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to edit Online Events.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EventEditCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the command results
		CommandResult result = ctx.getResult();
		
		// Save the airport list
		Set<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
		airports.addAll(SystemData.getAirports().values());
		ctx.setAttribute("airports", airports, REQUEST);
		
		// Save network names
		ctx.setAttribute("networks", SystemData.getObject("online.networks"), REQUEST);
		
		// Get the event ID - if not found, assume a new event
		if (ctx.getID() == 0) {
			EventAccessControl access = new EventAccessControl(ctx, new Event(""));
			access.validate();
			if (!access.getCanCreate())
				throw securityException("Cannot create new Online Event");
			
			try {
				Connection con = ctx.getConnection();
				
				// Get aircraft types
				GetAircraft acdao = new GetAircraft(con);
				ctx.setAttribute("allEQ", acdao.getAircraftTypes(), REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
			
			// Save the access controller
			ctx.setAttribute("access", access, REQUEST);
			
			// Redirect to the JSP
			result.setURL("/jsp/event/eventEdit.jsp");
			result.setSuccess(true);
			return;
		}
		
		Event e = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the event
			GetEvent dao = new GetEvent(con);
			e = dao.get(ctx.getID());
			if (e == null)
				throw notFoundException("Invalid Online Event - " + ctx.getID());
			
			// Calculate our access to the event
			EventAccessControl access = new EventAccessControl(ctx, e);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot edit Online Event");
			
			// Get all of the charts for this event
			GetChart cdao = new GetChart(con);
			Map<Airport, Collection<Chart>> charts = new TreeMap<Airport, Collection<Chart>>();
			for (Iterator<Airport> i = e.getAirports().iterator(); i.hasNext(); ) {
				Airport a = i.next();
				List<Chart> aCharts = cdao.getCharts(a);
				if (!aCharts.isEmpty())
				   charts.put(a, aCharts);
			}
			
			// Get the selected charts
			e.addCharts(cdao.getChartsByEvent(e.getID()));
			
			// Get aircraft types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("allEQ", acdao.getAircraftTypes(), REQUEST);
			
			// Save the charts
			if (!charts.isEmpty()) {
			   ctx.setAttribute("charts", charts, REQUEST);
			   ctx.setAttribute("chartAirports", charts.keySet(), REQUEST);
			}
			
			// Save the access controller
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Get the user's local time zone, and the server timezone
		TZInfo tz = ctx.getUser().getTZ();
		
		// Convert the dates to local time for the input fields
		ctx.setAttribute("event", e, REQUEST);
		ctx.setAttribute("startTime", DateTime.convert(e.getStartTime(), tz), REQUEST);
		ctx.setAttribute("endTime", DateTime.convert(e.getEndTime(), tz), REQUEST);
		ctx.setAttribute("signupDeadline", DateTime.convert(e.getSignupDeadline(), tz), REQUEST);
		
		// Forward to the JSP
		ctx.setAttribute("eventID", StringUtils.formatHex(ctx.getID()), REQUEST);
		result.setURL("/jsp/event/eventEdit.jsp");
		result.setSuccess(true);
	}
}