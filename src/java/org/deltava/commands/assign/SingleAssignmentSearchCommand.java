// Copyright 2008, 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.assign.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to search the schedule to build a flight assignment that consists of a single leg selected at
 * random from the last Airport the Pilot completed a flight to in the selected aircraft.
 * @author Luke
 * @version 3.6
 * @since 2.2
 */

public class SingleAssignmentSearchCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the result
		CommandResult result = ctx.getResult();
		
		// Build the search criteria
		ScheduleSearchCriteria criteria = new ScheduleSearchCriteria("RAND()");
		criteria.setDBName(SystemData.get("airline.db"));
		criteria.setMaxResults(1);

		// Get total legs to load
		int totalLegs = Math.min(8, Math.max(1, StringUtils.parse(ctx.getParameter("legs"), 1)));
		try {
			Connection con = ctx.getConnection();

			// Check if we have any other open flight assignments
			boolean hasOpen = false;
			GetAssignment dao = new GetAssignment(con);
			List<AssignmentInfo> assignments = dao.getByPilot(ctx.getUser().getID());
			for (Iterator<AssignmentInfo> i = assignments.iterator(); i.hasNext() && !hasOpen;) {
				AssignmentInfo a = i.next();
				hasOpen = hasOpen || (a.getStatus() == AssignmentInfo.RESERVED);
			}

			// If we have an open assignment, abort
			if (hasOpen) {
				ctx.release();
				result.setURL("/jsp/assign/assignOpen.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Get the last airport we flew into
			GetFlightReports prdao = new GetFlightReports(con);
			prdao.setQueryMax(10);
			List<FlightReport> pireps = prdao.getByPilot(ctx.getUser().getID(), new ScheduleSearchCriteria("ID DESC"));
			for (Iterator<FlightReport> i = pireps.iterator(); (criteria.getAirportD() == null) && i.hasNext();) {
				FlightReport fr = i.next();
				if ((fr.getStatus() != FlightReport.DRAFT) && (fr.getStatus() != FlightReport.REJECTED))
					criteria.setAirportD(fr.getAirportA());
			}

			// If no last airport, abort
			if (criteria.getAirportD() == null)
				throw notFoundException("No flights logged");
			
			// Save the user
			ctx.setAttribute("pilot", ctx.getUser(), REQUEST);
			ctx.setAttribute("airlines", SystemData.getAirlines().values(), REQUEST);

			// Get additional parameters if we are redoing a search
			if (ctx.getParameter("eqType") != null) {
				criteria.setEquipmentType(ctx.getParameter("eqType"));
				criteria.setAirline(SystemData.getAirline(ctx.getParameter("airline")));
			}
			
			// Define Variables
			AssignmentInfo ai = null; Airport startAirport = criteria.getAirportD();
			Collection<ScheduleEntry> flightEntries = new ArrayList<ScheduleEntry>();
			
			// Load the schedule entries - allow multiple legs
			GetSchedule sdao = new GetSchedule(con);
			sdao.setQueryMax(1);
			for (int x = 0; x < totalLegs; x++) {
				List<ScheduleEntry> legs = sdao.search(criteria);	
				if (ai == null) {
					String eqType = legs.isEmpty() ? "RANDOM" : legs.get(0).getEquipmentType();
					ai = new AssignmentInfo(eqType);
				}
				
				// Add the leg
				if (!legs.isEmpty()) {
					ScheduleEntry entry = legs.get(0);
					ai.addAssignment(new AssignmentLeg(entry));
					DraftFlightReport fr = new DraftFlightReport(entry);
					fr.setTimeD(entry.getTimeD());
					fr.setTimeA(entry.getTimeA());
					fr.setAttribute(FlightReport.ATTR_HISTORIC, entry.getHistoric());
					ai.addFlight(fr);
					flightEntries.add(entry);
					
					// Set new starting airport
					criteria.setAirportD(entry.getAirportA());
				} else
					break;
			}
			
			// Restore starting airport
			criteria.setAirportD(startAirport);
				
			// Save in the seession
			ctx.setAttribute("assign", ai, SESSION);
			ctx.setAttribute("entries", flightEntries, REQUEST);
			ctx.setAttribute("criteria", criteria, REQUEST);
			ctx.setAttribute("totalLegs", Integer.valueOf(totalLegs), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setURL("/jsp/assign/singleAssignment.jsp");
		result.setSuccess(true);
	}
}