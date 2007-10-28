// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.event.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.*;

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
			List<Event> results = edao.getFutureEvents();
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
				Event e = results.get(0);
				eventID = e.getID();
			}

			// Load the event. We reload since getFutureEvents() does not populate child lists
			Event e = edao.get(eventID);
			if (e == null)
				throw notFoundException("Invalid Online Event - " + eventID);

			// Calculate our access
			EventAccessControl eAccess = new EventAccessControl(ctx, e);
			eAccess.validate();
			
			// If we can sign up, save us in the request
			if (eAccess.getCanSignup())
				ctx.setAttribute("user", ctx.getUser(), REQUEST);

			// Set access on the signups
			Map<Integer, SignupAccessControl> sAccessMap = new HashMap<Integer, SignupAccessControl>();
			for (Iterator<Signup> i = e.getSignups().iterator(); i.hasNext(); ) {
				Signup s = i.next();
				SignupAccessControl sAccess = new SignupAccessControl(ctx, e, s);
				sAccess.validate();
				sAccessMap.put(new Integer(s.getPilotID()), sAccess);
			}
			
			// Get the DAO and load the Charts
			GetChart cdao = new GetChart(con);
			e.addCharts(cdao.getChartsByEvent(e.getID()));
			
			// Get the equipment types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("allEQ", acdao.getAircraftTypes(), REQUEST);
			
			// Get the location of all the pilots
			GetUserData usrdao = new GetUserData(con);
			UserDataMap udm = usrdao.getByEvent(e.getID());
			ctx.setAttribute("userData", udm, REQUEST);
			
			// Get the DAOs
			GetPilot pdao = new GetPilot(con);
			GetFlightReports frdao = new GetFlightReports(con);
			GetAcademyCourses crsdao = new GetAcademyCourses(con);
			
			// Load the Pilots and Flight Reports
			Collection<FlightReport> pireps = new ArrayList<FlightReport>();
			Map<Integer, Pilot> pilots = new HashMap<Integer, Pilot>();
			Map<Integer, Collection<String>> certs = new HashMap<Integer, Collection<String>>();
			for (Iterator<String> i = udm.getTableNames().iterator(); i.hasNext(); ) {
				String tableName = i.next();
				Collection<UserData> ids = udm.getByTable(tableName);
				Collection<FlightReport> flights = frdao.getByEvent(e.getID(), tableName);
				frdao.getCaptEQType(flights);
				pireps.addAll(flights);
				
				// Load pilots who may have logged the flight but not signed up
				Collection<Integer> newIDs = new HashSet<Integer>();
				for (Iterator<FlightReport> fi =flights.iterator(); fi.hasNext(); ) {
					FlightReport fr = fi.next();
					int pilotID = fr.getDatabaseID(FlightReport.DBID_PILOT);
					if (!udm.contains(pilotID))
						newIDs.add(new Integer(pilotID));
				}
				
				// Load additional pilots
				if (!newIDs.isEmpty())
					pilots.putAll(pdao.getByID(newIDs, tableName));
				
				// Load Flight Academy Certifications
				certs.putAll(crsdao.getCertifications(ids));
			}
			
			// Save the pilots and flight reports
			ctx.setAttribute("pilots", pdao.get(udm), REQUEST);
			ctx.setAttribute("pireps", pireps, REQUEST);
			ctx.setAttribute("certs", certs, REQUEST);

			// Save event info in the request
			ctx.setAttribute("event", e, REQUEST);
			ctx.setAttribute("access", eAccess, REQUEST);
			ctx.setAttribute("saAccess", sAccessMap, REQUEST);
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