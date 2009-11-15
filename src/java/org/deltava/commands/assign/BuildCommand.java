// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.assign.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to build a Flight Assignment.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class BuildCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command results and set the default return operation
		CommandResult result = ctx.getResult();

		// If we're adding flights to the in-session assignment
		List<?> results = (List<?>) ctx.getSession().getAttribute("fafResults");
		Collection<String> ids = ctx.getParameters("addFA");
		if ((ids == null) || (results == null)) {
			result.setURL("/jsp/schedule/findAflight.jsp");
			result.setSuccess(true);
			return;
		}

		// Get the list of results and split into two - the selected, and those remaining
		List<Flight> fList = new ArrayList<Flight>();
		for (Iterator<?> i = results.iterator(); i.hasNext();) {
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
				FlightReport fr = new FlightReport(f);
				
				// Copy arrival/departure times
				if (f instanceof ScheduleEntry) {
					ScheduleEntry se = (ScheduleEntry) f;
					StringBuilder buf = new StringBuilder("Scheduled departure at ");
					buf.append(StringUtils.format(se.getTimeD(), ctx.getUser().getTimeFormat()));
					buf.append(' ');
					buf.append(se.getAirportD().getTZ());
					buf.append(", scheduled arrival at ");
					buf.append(StringUtils.format(se.getTimeA(), ctx.getUser().getTimeFormat()));
					buf.append(' ');
					buf.append(se.getAirportA().getTZ());
					fr.setRemarks(buf.toString());
				}
				
				info.addFlight(fr);
			}

			ctx.setAttribute("buildAssign", info, SESSION);
		}

		// Load the airports from the criteria
		ScheduleSearchCriteria criteria = (ScheduleSearchCriteria) ctx.getSession().getAttribute("fafCriteria");
		if (criteria != null) {
			try {
				// Get departure/arrival airports
				GetScheduleAirport adao = new GetScheduleAirport(ctx.getConnection());
				ctx.setAttribute("airports", adao.getOriginAirports(criteria.getAirline()), REQUEST);
				ctx.setAttribute("airportsA", adao.getConnectingAirports(criteria.getAirportD(), true, null), REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		}

		// Get Airlines
		Collection<Airline> airlines = new LinkedHashSet<Airline>(SystemData.getAirlines().values());
		for (Iterator<Airline> i = airlines.iterator(); i.hasNext();) {
			Airline al = i.next();
			if (!al.getActive())
				i.remove();
		}

		// Save airlines
		ctx.setAttribute("airlines", airlines, REQUEST);

		// Set combo variables for JSP
		ctx.setAttribute("sortTypes", ScheduleSearchCriteria.SORT_OPTIONS, REQUEST);
		ctx.setAttribute("hours", ScheduleSearchCriteria.HOURS, REQUEST);

		// Redirect back to the find-a-flight page
		result.setURL("/jsp/schedule/findAflight.jsp");
		result.setSuccess(true);
		return;
	}
}