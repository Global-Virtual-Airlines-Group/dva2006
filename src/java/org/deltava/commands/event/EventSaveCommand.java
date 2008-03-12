// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.event.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.EventAccessControl;

import org.deltava.util.*;
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

		// Check if this is a new event
		boolean isNew = (ctx.getID() == 0);
		if (!isNew)
			ctx.setAttribute("eventID", StringUtils.formatHex(ctx.getID()), REQUEST);

		// Initialize the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the online event
			Event e = null;
			if (!isNew) {
				GetEvent dao = new GetEvent(con);
				e = dao.get(ctx.getID());
				if (e == null)
					throw notFoundException("Invalid Online Event - " + ctx.getID());

				e.setName(ctx.getParameter("name"));
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

			// Populate fields from the request
			e.setNetwork(ctx.getParameter("network"));
			e.setBriefing(ctx.getParameter("briefing"));
			e.setCanSignup(Boolean.valueOf(ctx.getParameter("canSignup")).booleanValue());

			// Parse the start/end/deadline times
			e.setStartTime(parseDateTime(ctx, "start", SystemData.get("time.date_format"), "HH:mm"));
			e.setEndTime(parseDateTime(ctx, "end", SystemData.get("time.date_format"), "HH:mm"));
			e.setSignupDeadline(e.getCanSignup() ? parseDateTime(ctx, "close", SystemData.get("time.date_format"), "HH:mm") : e.getStartTime());
			
			// Load initial flight route
			if (e.getCanSignup() && (ctx.getParameter("route") != null)) {
				Route r = new Route(0, ctx.getParameter("route"));
				r.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
				r.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
				r.setMaxSignups(StringUtils.parse(ctx.getParameter("maxSignups"), 0));
				r.setIsRNAV(Boolean.valueOf(ctx.getParameter("isRNAV")).booleanValue());
				r.setName(ctx.getParameter("routeName"));
				r.setRouteID(1);
				r.setActive(true);
				e.addRoute(r);
				
				// Add to the message context
				mctxt.addData("route", r);
			}

			// Parse the equipment types
			Collection<String> eqTypes = ctx.getParameters("eqTypes");
			if (eqTypes != null) {
				e.getEquipmentTypes().clear();
				for (Iterator<String> i = eqTypes.iterator(); i.hasNext(); )
					e.addEquipmentType(i.next());
			}

			// See which charts have been selected
			Collection<String> selectedCharts = ctx.getParameters("charts");
			if (selectedCharts != null) {
				Collection<Integer> chartIDs = new HashSet<Integer>();
				for (Iterator<String> i = selectedCharts.iterator(); i.hasNext(); )
					chartIDs.add(new Integer(StringUtils.parseHex(i.next())));

				// Load the charts
				e.getCharts().clear();
				GetChart cdao = new GetChart(con);
				e.addCharts(cdao.getByIDs(chartIDs));
			}
			
			// Parse contact addresses
			Collection<String> addrs = StringUtils.split(ctx.getParameter("contactAddrs"), "\n");
			if (!CollectionUtils.isEmpty(addrs)) {
				e.getContactAddrs().clear();
				for (Iterator<String> i = addrs.iterator(); i.hasNext(); )
					e.addContactAddr(i.next());
			}

			// Save the event in the request
			ctx.setAttribute("event", e, REQUEST);
			
			// Write the event
			SetEvent wdao = new SetEvent(con);
			wdao.write(e);

			// Get the DAO and save the event if we're not refreshing
			if (isNew) {
				// Get the message template
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("EVENTCREATE"));
				mctxt.addData("event", e);
				mctxt.setSubject("Online Event - " + e.getName());

				// Save the start/end/signup dates
				mctxt.addData("startDateTime", StringUtils.format(e.getStartTime(), "MM/dd/yyyy HH:mm"));
				mctxt.addData("endDateTime", StringUtils.format(e.getEndTime(), "MM/dd/yyyy HH:mm"));
				mctxt.addData("signupDeadline", StringUtils.format(e.getSignupDeadline(), "MM/dd/yyyy HH:mm"));

				// Get the Pilots to notify
				GetPilotNotify pdao = new GetPilotNotify(con);
				Collection<EMailAddress> pilots = pdao.getNotifications(Person.EVENT);
				if (pilots != null) {
					for (Iterator<String> i = e.getContactAddrs().iterator(); i.hasNext(); )
						pilots.add(Mailer.makeAddress(i.next()));
					
					Mailer mailer = new Mailer(ctx.getUser());
					mailer.setContext(mctxt);
					mailer.send(pilots);
				}
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/event/eventUpdate.jsp");
		result.setSuccess(true);
	}
}