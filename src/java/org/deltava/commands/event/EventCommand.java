// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.event.*;
import org.deltava.beans.system.UserDataMap;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EventAccessControl;
import org.deltava.security.command.SignupAccessControl;

/**
 * A Web Site Command to display an Online Event.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EventCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get command results
		CommandResult result = ctx.getResult();

		try {
			Connection con = ctx.getConnection();

			// Get the database ID
			int eventID = ctx.getID();

			// Get the DAO. If no ID specified, get the ID of the next event
			GetEvent edao = new GetEvent(con);
			if (eventID == 0) {
				edao.setQueryMax(1);
				List results = edao.getFutureEvents();

				// If no future events, redirect to the no events page - make sure we calculate event access first
				if (results.isEmpty())
					results = edao.getEvents();

				if (results.isEmpty()) {
					EventAccessControl eAccess = new EventAccessControl(ctx, null);
					eAccess.validate();
					ctx.setAttribute("access", eAccess, REQUEST);

					result.setURL("/jsp/event/noActiveEvents.jsp");
					result.setSuccess(true);
					return;
				}

				// Get the ID of the next event
				Event e = (Event) results.get(0);
				eventID = e.getID();
			}

			// Load the event. We reload since getFutureEvents() does not populate child lists
			Event e = edao.get(eventID);
			if (e == null)
				throw new CommandException("Invalid Online Event - " + eventID);

			// Calculate our access
			EventAccessControl eAccess = new EventAccessControl(ctx, e);
			eAccess.validate();
			
			// If we can sign up, save us in the request
			if (eAccess.getCanSignup())
				ctx.setAttribute("user", ctx.getUser(), REQUEST);

			// Set access on the signups
			List sAccessList = new ArrayList();
			for (Iterator i = e.getSignups().iterator(); i.hasNext(); ) {
				Signup s = (Signup) i.next();
				SignupAccessControl sAccess = new SignupAccessControl(ctx, e, s);
				sAccess.validate();
				sAccessList.add(sAccess);
			}
			
			// Get the DAO and load the Charts
			GetChart cdao = new GetChart(con);
			e.addCharts(cdao.getChartsByEvent(e.getID()));

			// Get the DAO and load the Flight Reports
			GetFlightReports frdao = new GetFlightReports(con);
			ctx.setAttribute("pireps", frdao.getByEvent(eventID), REQUEST);
			
			// Get the location of all the pilots
			GetUserData usrdao = new GetUserData(con);
			UserDataMap udm = usrdao.getByEvent(e.getID());
			ctx.setAttribute("userData", udm, REQUEST);

			// Get the DAO and load the Pilots
			Map pilots = new HashMap();
			GetPilot pdao = new GetPilot(con);
			for (Iterator i = udm.getTableNames().iterator(); i.hasNext(); ) {
				String tableName = (String) i.next();
				Set pilotIDs = new HashSet(udm.getByTable(tableName));
				pilots.putAll(pdao.getByID(pilotIDs, tableName));
			}
			
			// Save the pilots
			ctx.setAttribute("pilots", pilots, REQUEST);

			// Save event info in the request
			ctx.setAttribute("event", e, REQUEST);
			ctx.setAttribute("access", eAccess, REQUEST);
			ctx.setAttribute("sAccess", sAccessList, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setURL("/jsp/event/eventView.jsp");
		result.setSuccess(true);
	}
}