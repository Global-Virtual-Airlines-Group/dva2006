// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.DateTime;
import org.deltava.beans.TZInfo;

import org.deltava.beans.event.Event;
import org.deltava.beans.schedule.Airport;
import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;

import org.deltava.dao.GetEvent;
import org.deltava.dao.GetChart;
import org.deltava.dao.DAOException;

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
		Set airports = new TreeSet(new AirportComparator(AirportComparator.NAME));
		airports.addAll(((Map) SystemData.getObject("airports")).values());
		ctx.setAttribute("airports", airports, REQUEST);
		
		// Strip out ACARS as a network name
		Set netNames = new TreeSet((List) SystemData.getObject("online.networks"));
		netNames.remove("ACARS");
		ctx.setAttribute("networks", netNames, REQUEST);
		
		// Get the event ID - if not found, assume a new event
		if (ctx.getID() == 0) {
			EventAccessControl access = new EventAccessControl(ctx, new Event(""));
			access.validate();
			if (!access.getCanCreate())
				throw securityException("Cannot create new Online Event");
			
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
				throw new CommandException("Invalid Online Event - " + ctx.getID());
			
			// Calculate our access to the event
			EventAccessControl access = new EventAccessControl(ctx, e);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot edit Online Event");
			
			// Get all of the charts for this event
			GetChart cdao = new GetChart(con);
			Map charts = new TreeMap();
			for (Iterator i = e.getAirports().iterator(); i.hasNext(); ) {
				Airport a = (Airport) i.next();
				List aCharts = cdao.getCharts(a);
				if (!aCharts.isEmpty())
				   charts.put(a, aCharts);
			}
			
			// Get the selected charts
			e.addCharts(cdao.getChartsByEvent(e.getID()));
			
			// Save the charts
			if (!charts.isEmpty()) {
			   ctx.setAttribute("charts", charts, REQUEST);
			   ctx.setAttribute("chartAirports", charts.keySet(), REQUEST);
			}
			
			// Build the airport list to save in the field
			StringBuffer buf = new StringBuffer();
			for (Iterator i = e.getAirportD().iterator(); i.hasNext();) {
				Airport a = (Airport) i.next();
				buf.append(a.getIATA());
				if (i.hasNext())
					buf.append(',');
			}

			// Save the airports
			ctx.setAttribute("adCodes", buf.toString(), REQUEST);
			
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