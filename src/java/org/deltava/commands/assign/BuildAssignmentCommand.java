// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.assign.*;
import org.deltava.beans.schedule.ScheduleSearchCriteria;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to build a Flight Assignment.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class BuildAssignmentCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command results and set the default return operation
		CommandResult result = ctx.getResult();

		// Figure out if we're just adding to or clearing the session attribute
		String opName = (String) ctx.getCmdParameter(Command.OPERATION, "build");

		// If we're reseting the session, clear stuff out and return back
		if ("reset".equals(opName)) {
			ctx.getSession().removeAttribute("buildAssign");
			ctx.getSession().removeAttribute("fafCriteria");
			ctx.getSession().removeAttribute("fafResults");

			// Redirect back to the find-a-flight command
			result.setType(CommandResult.REDIRECT);
			result.setURL("findflight.do");
			result.setSuccess(true);
			return;
		}

		// If we're adding flights to the in-session assignment
		if ("build".equals(opName)) {
			List results = (List) ctx.getSession().getAttribute("fafResults");
			Collection<String> ids = ctx.getParameters("addFA");
			if ((ids == null) || (results == null)) {
				result.setURL("/jsp/schedule/findAflight.jsp");
				result.setSuccess(true);
				return;
			}

			// Get the list of results and split into two - the selected, and those remaining
			List<Flight> fList = new ArrayList<Flight>();
			for (Iterator i = results.iterator(); i.hasNext();) {
				Flight f = (Flight) i.next();
				if (ids.contains(f.getFlightCode())) {
					fList.add(f);
					i.remove();
				}
			}

			// Build the flight assignment and save in the session
			if (!fList.isEmpty()) {
				// Get the first flight for the eq type
				Flight ff = fList.get(0);
				AssignmentInfo info = (AssignmentInfo) ctx.getSession().getAttribute("buildAssign");
				
				// Create the assignment if it doesn't exist
				if (info == null) {
					info = new AssignmentInfo(ff.getEquipmentType());
					info.setPilotID(ctx.getUser());
					info.setStatus(AssignmentInfo.RESERVED);
					info.setRandom(true);
					info.setPurgeable(true);
					info.setAssignDate(new Date());
				}

				// Populate the legs
				for (Iterator<Flight> i = fList.iterator(); i.hasNext();) {
					Flight f = i.next();
					info.addAssignment(new AssignmentLeg(f));
					info.addFlight(new FlightReport(f));
				}

				ctx.setAttribute("buildAssign", info, SESSION);
			}
			
			// Load the airports from the criteria
			ScheduleSearchCriteria criteria = (ScheduleSearchCriteria) ctx.getSession().getAttribute("fafCriteria");
			if (criteria != null) {
				try {
					Connection con = ctx.getConnection();
					
					// Get departure/arrival airports
					GetScheduleAirport adao = new GetScheduleAirport(con);
					ctx.setAttribute("airports", adao.getOriginAirports(criteria.getAirline()), REQUEST);
					if (criteria.getAirportD() != null)
						ctx.setAttribute("airportsA", adao.getConnectingAirports(criteria.getAirportD(), true), REQUEST);
				} catch (DAOException de) {
					throw new CommandException(de);
				} finally {
					ctx.release();
				}
			}
			
			// Set combo variables for JSP
			ctx.setAttribute("sortTypes", ScheduleSearchCriteria.SORT_OPTIONS, REQUEST);
			ctx.setAttribute("hours", ScheduleSearchCriteria.HOURS, REQUEST);
			
			// Redirect back to the find-a-flight page
			result.setURL("/jsp/schedule/findAflight.jsp");
			result.setSuccess(true);
			return;
		}

		// If we got this far, it's an unknown opName
		CommandException ce = new CommandException("Invalid Operation - " + opName);
		ce.setLogStackDump(false);
		throw ce;
	}
}