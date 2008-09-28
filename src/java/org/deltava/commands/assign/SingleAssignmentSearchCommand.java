// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FlightReport;
import org.deltava.beans.assign.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to search the schedule to build a flight assignment that consists of a single leg selected at
 * random from the last Airport the Pilot completed a flight to in the selected aircraft.
 * @author Luke
 * @version 2.2
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
				if ((fr.getStatus() != FlightReport.DRAFT) && (fr.getStatus() != FlightReport.REJECTED)) {
					criteria.setAirportD(fr.getAirportA());
					criteria.setIncludeHistoric(fr.hasAttribute(FlightReport.ATTR_HISTORIC));
				}
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
				criteria.setIncludeHistoric(Boolean.valueOf(ctx.getParameter("includeHistoric")).booleanValue());
			}
			
			// Load the schedule entries
			GetSchedule sdao = new GetSchedule(con);
			List<ScheduleEntry> entries = sdao.search(criteria, "1");
			String eqType = entries.isEmpty() ? "RANDOM" : entries.get(0).getEquipmentType();
			AssignmentInfo ai = new AssignmentInfo(eqType);
			if (!entries.isEmpty()) {
				ScheduleEntry entry = entries.get(0);
				ai.addAssignment(new AssignmentLeg(entry));
				FlightReport fr = new FlightReport(entry);
				fr.setAttribute(FlightReport.ATTR_HISTORIC, entry.getHistoric());
				ai.addFlight(fr);
			}
				
			// Save in the seession
			ctx.setAttribute("assign", ai, SESSION);
			ctx.setAttribute("entries", entries, REQUEST);
			ctx.setAttribute("criteria", criteria, REQUEST);
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