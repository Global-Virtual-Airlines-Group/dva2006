// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.assign.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to build a Flight Assignment.
 * @author Luke
 * @version 4.1
 * @since 1.0
 */

public class BuildCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command results and set the default return operation
		CommandResult result = ctx.getResult();

		// If we're adding flights to the in-session assignment
		List<?> results = (List<?>) ctx.getSession().getAttribute("fafResults");
		Collection<String> ids = ctx.getParameters("addFA");
		if ((ids == null) || (results == null)) {
			result.setURL("findflight", null, 0);
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
			
			// Get equipment override
			String eqOv = ctx.getParameter("eqOverride");
			if ((eqOv != null) && (eqOv.length() < 3))
				eqOv = null;

			// Populate the legs
			for (Flight f : fList) {
				info.addAssignment(new AssignmentLeg(f));
				DraftFlightReport fr = new DraftFlightReport(f);
				if (eqOv != null)
					fr.setEquipmentType(eqOv);
				
				// Copy arrival/departure times
				if (f instanceof ScheduleEntry) {
					ScheduleEntry se = (ScheduleEntry) f;
					fr.setTimeD(se.getTimeD());
					fr.setTimeA(se.getTimeA());
					fr.setAttribute(FlightReport.ATTR_HISTORIC, se.getHistoric());
					fr.setRemarks(fr.getDraftComments());
				}
				
				info.addFlight(fr);
			}

			ctx.setAttribute("buildAssign", info, SESSION);
		}

		// Load the airports from the criteria
		ScheduleSearchCriteria criteria = (ScheduleSearchCriteria) ctx.getSession().getAttribute("fafCriteria");
		try {
			Connection con = ctx.getConnection();
				
			// Get departure/arrival airports
			if (criteria != null) {
				GetScheduleAirport adao = new GetScheduleAirport(con);
				ctx.setAttribute("airports", adao.getOriginAirports(criteria.getAirline()), REQUEST);
				ctx.setAttribute("airportsA", adao.getConnectingAirports(criteria.getAirportD(), true, null), REQUEST);
			}
				
			// Get the equipment types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("allEQ", acdao.getAircraftTypes(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Get Airlines
		Collection<Airline> airlines = new LinkedHashSet<Airline>(SystemData.getAirlines().values());
		for (Iterator<Airline> i = airlines.iterator(); i.hasNext();) {
			Airline al = i.next();
			if (!al.getActive())
				i.remove();
		}

		// Save airlines and combo variables for JSP
		ctx.setAttribute("airlines", airlines, REQUEST);
		ctx.setAttribute("sortTypes", ScheduleSearchCriteria.SORT_OPTIONS, REQUEST);
		ctx.setAttribute("hours", ScheduleSearchCriteria.HOURS, REQUEST);
		ctx.setAttribute("myEQ", ctx.getUser().getRatings(), REQUEST);

		// Redirect back to the find-a-flight page
		result.setURL("/jsp/schedule/findAflight.jsp");
		result.setSuccess(true);
	}
}