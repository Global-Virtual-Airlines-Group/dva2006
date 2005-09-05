// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Person;
import org.deltava.beans.event.Event;
import org.deltava.beans.schedule.Airport;
import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.EventAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to save Online Events.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EventSaveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Check if we are refreshing and if this is a new event
		boolean isRefresh = "refresh".equals(ctx.getCmdParameter(Command.OPERATION, null));
		boolean isNew = (ctx.getID() == 0);

		// Initialize the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		List pilots = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the online event
			Event e = null;
			if (!isNew) {
				GetEvent dao = new GetEvent(con);
				e = dao.get(ctx.getID());
				if (e == null)
					throw new CommandException("Invalid Online Event - " + ctx.getID());

				ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);
			} else {
				e = new Event(ctx.getParameter("name"));
				ctx.setAttribute("isNew", Boolean.TRUE, REQUEST);
			}

			// Check our access
			EventAccessControl access = new EventAccessControl(ctx, e);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot edit Online Event");

			// Load previous airports
			if (ctx.getParameter("airportDCodes") != null) {
				StringTokenizer tokens = new StringTokenizer(ctx.getParameter("airportDCodes"), ",");
				while (tokens.hasMoreTokens())
					e.addAirportD(SystemData.getAirport(tokens.nextToken()));
			}

			// Populate fields from the request
			e.setNetwork(ctx.getParameter("network"));
			e.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
			e.addAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
			e.setRoute(ctx.getParameter("route"));
			e.setBriefing(ctx.getParameter("briefing"));

			// Parse the start/end/deadline times
			e.setStartTime(parseDateTime(ctx, "start"));
			e.setEndTime(parseDateTime(ctx, "end"));
			e.setSignupDeadline(parseDateTime(ctx, "close"));

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

			// Get the remaining airports and save in the request
			Set airports = new TreeSet(new AirportComparator(AirportComparator.NAME));
			airports.addAll(((Map) SystemData.getObject("airports")).values());
			airports.removeAll(e.getAirportD());
			ctx.setAttribute("airports", airports, REQUEST);

			// Get all of the charts for this event
			GetChart cdao = new GetChart(con);
			Map charts = new TreeMap();
			for (Iterator i = e.getAirports().iterator(); i.hasNext(); ) {
				Airport a = (Airport) i.next();
				List aCharts = cdao.getCharts(a);
				if (!aCharts.isEmpty())
				   charts.put(a, aCharts);
			}
			
			// Save the charts
			if (!charts.isEmpty()) {
			   ctx.setAttribute("charts", charts, REQUEST);
			   ctx.setAttribute("chartAirports", charts.keySet(), REQUEST);
			}

			// Save the charts and the event in the request
			ctx.setAttribute("access", access, REQUEST);
			ctx.setAttribute("event", e, REQUEST);

			// Get the DAO and save the event if we're not refreshing
			if ((!isRefresh) && (isNew)) {
				// Get the message template
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("EVENTCREATE"));
				mctxt.addData("event", e);

				// Save the start/end/signup dates
				mctxt.addData("airports", StringUtils.listConcat(e.getAirportD(), ","));
				mctxt.addData("startDateTime", StringUtils.format(e.getStartTime(), "MM/dd/yyyy HH:mm"));
				mctxt.addData("endDateTime", StringUtils.format(e.getEndTime(), "MM/dd/yyyy HH:mm"));
				mctxt.addData("signupDeadline", StringUtils.format(e.getSignupDeadline(), "MM/dd/yyyy HH:mm"));

				// Get the Pilots to notify
				GetPilotNotify pdao = new GetPilotNotify(con);
				pilots = pdao.getNotifications(Person.EVENT);

				// Write the event
				SetEvent wdao = new SetEvent(con);
				wdao.write(e);
			} else if (!isRefresh) {
				SetEvent wdao = new SetEvent(con);
				wdao.write(e);
			} else {
				// Strip out ACARS as a network name
				Set netNames = new TreeSet((List) SystemData.getObject("online.networks"));
				netNames.remove("ACARS");
				ctx.setAttribute("networks", netNames, REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Send the e-mail notification
		if (pilots != null) {
			Mailer mailer = new Mailer(ctx.getUser());
			mailer.setContext(mctxt);
			mailer.send(pilots);
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setSuccess(true);
		if (isRefresh) {
			result.setURL("/jsp/event/eventEdit.jsp");
		} else {
			result.setType(CommandResult.REQREDIRECT);
			result.setURL("/jsp/event/eventUpdate.jsp");
		}
	}
}