// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2012, 2014, 2015, 2017, 2021, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.event.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.acars.DispatchRoute;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.*;

import org.deltava.util.CollectionUtils;

/**
 * A Web Site Command to display an Online Event.
 * @author Luke
 * @version 11.6
 * @since 1.0
 */

public class EventCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
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

			// Calculate our access - if we can sign up, save us in the request
			EventAccessControl eAccess = new EventAccessControl(ctx, e);
			eAccess.validate();
			if (eAccess.getCanSignup())
				ctx.setAttribute("user", ctx.getUser(), REQUEST);

			// Set access on the signups
			Map<Integer, SignupAccessControl> sAccessMap = new HashMap<Integer, SignupAccessControl>();
			for (Signup s : e.getSignups()) {
				SignupAccessControl sAccess = new SignupAccessControl(ctx, e, s);
				sAccess.validate();
				sAccessMap.put(Integer.valueOf(s.getPilotID()), sAccess);
			}
			
			// Get the DAO and load the Charts
			GetChart cdao = new GetChart(con);
			e.addCharts(cdao.getChartsByEvent(e.getID()));
			
			// Get dispatch routes
			GetACARSRoute ardao = new GetACARSRoute(con);
			for (Route r : e.getActiveRoutes()) {
				Collection<DispatchRoute> rts = ardao.getRoutes(r, true);
				e.getDispatchRoutes().addAll(rts);
			}
			
			// Get the equipment types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("allEQ", acdao.getAircraftTypes(), REQUEST);
			
			// Get the location of all the pilots
			GetUserData usrdao = new GetUserData(con);
			UserDataMap udm = usrdao.getByEvent(e.getID());
			ctx.setAttribute("userData", udm, REQUEST);
			
			// Get the DAOs
			GetPilot pdao = new GetPilot(con);
			GetEventSignups evsdao = new GetEventSignups(con);
			GetFlightReports frdao = new GetFlightReports(con);
			GetAcademyCourses crsdao = new GetAcademyCourses(con);
			GetAcademyCertifications crtdao = new GetAcademyCertifications(con);
			ctx.setAttribute("allCerts", crtdao.getAll(), REQUEST);
			
			// Load the Pilots and Flight Reports
			Collection<FlightReport> pireps = new ArrayList<FlightReport>();
			Map<Integer, Pilot> pilots = new HashMap<Integer, Pilot>();
			Map<Integer, Collection<String>> certs = new HashMap<Integer, Collection<String>>();
			for (String tableName : udm.getTableNames()) {
				Collection<UserData> ids = udm.getByTable(tableName);
				Collection<FlightReport> flights = frdao.getByEvent(e.getID(), tableName);
				frdao.getCaptEQType(flights);
				pireps.addAll(flights);
				
				// Load pilots signed up for the event
				Collection<Integer> flightAuthorIDs = flights.stream().map(AuthoredBean::getAuthorID).filter(id -> !udm.containsKey(id)).collect(Collectors.toSet());
				Map<Integer, Pilot> evPilots = pdao.getByID(ids, tableName);
				evPilots.putAll(pdao.getByID(flightAuthorIDs, tableName));
				
				// Load pilots who may have logged the flight but not signed up
				if ((e.getStatus() == Status.COMPLETE) || (e.getStatus() == Status.CLOSED)) {
					Collection<Integer> newIDs = flights.stream().filter(fr -> !udm.contains(fr.getAuthorID())).map(FlightReport::getAuthorID).collect(Collectors.toSet());
					if (!newIDs.isEmpty()) {
						udm.putAll(usrdao.get(newIDs));
						evPilots.putAll(pdao.getByID(newIDs, tableName));
					}
				}
				
				// Load event stats and save pilots
				frdao.getOnlineTotals(evPilots, tableName);
				evsdao.getSignupTotals(evPilots);
				pilots.putAll(evPilots);
				
				// Load Flight Academy Certifications
				certs.putAll(crsdao.getCertifications(ids, true));
			}
			
			// Load VATSIM Ratings
			ctx.setAttribute("allRatings", Collections.emptyMap(), REQUEST);
			
			// Calculate attendance probability
			if (e.getStatus() != Status.CANCELED) {
				float predictedPilots = 0;
				for (Signup s : e.getSignups()) {
					Pilot p = pilots.get(Integer.valueOf(s.getPilotID()));
					if ((p != null) && (p.getEventSignups() > 2))
						predictedPilots += Math.min(1.0d, (p.getEventLegs() * 1.0d) / p.getEventSignups());
					else
						predictedPilots += 0.4;
				}
				
				ctx.setAttribute("signupPredict", Integer.valueOf(Math.round(predictedPilots)), REQUEST); 
			}
			
			// Save the pilots and flight reports
			ctx.setAttribute("pilots", pilots, REQUEST);
			ctx.setAttribute("pireps", pireps, REQUEST);
			ctx.setAttribute("certs", certs, REQUEST);

			// Save event info in the request
			ctx.setAttribute("event", e, REQUEST);
			ctx.setAttribute("fbScore", FeedbackScore.generate(e), REQUEST);
			ctx.setAttribute("hasFB", Boolean.valueOf(ctx.isAuthenticated() && e.hasFeedback(ctx.getUser().getID())), REQUEST);
			ctx.setAttribute("access", eAccess, REQUEST);
			ctx.setAttribute("saAccess", sAccessMap, REQUEST);			
			// Save the routes in a map, keyed by ID
			ctx.setAttribute("routes", CollectionUtils.createMap(e.getRoutes(), Route::getRouteID), REQUEST);
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