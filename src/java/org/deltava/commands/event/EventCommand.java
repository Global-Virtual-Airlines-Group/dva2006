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

		// Get command results and event ID
		CommandResult result = ctx.getResult();
		int eventID = ctx.getID();

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and all future events
			GetEvent edao = new GetEvent(con);
			List results = edao.getFutureEvents();
			ctx.setAttribute("futureEvents", results, REQUEST);
			
			// If no event scheduled, then display a status page
			if (eventID == 0) {
				EventAccessControl eAccess = new EventAccessControl(ctx, null);
				eAccess.validate();
				ctx.setAttribute("access", eAccess, REQUEST);
				
				// If no future events, display the "No Events" page
				if (results.isEmpty()) {
				   ctx.release();

					result.setURL("/jsp/event/noActiveEvents.jsp");
					result.setSuccess(true);
					return;
				} else if (results.size() > 1) {
					ctx.release();
					
					result.setURL("/jsp/event/multipleEvents.jsp");
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
				pilots.putAll(pdao.getByID(udm.getByTable(tableName), tableName));
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