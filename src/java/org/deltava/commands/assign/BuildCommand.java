// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2015, 2016, 2017, 2019, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.time.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

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
 * @version 11.0
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
		HttpSession s = ctx.getSession();

		// If we're adding flights to the in-session assignment
		List<?> results = (List<?>) s.getAttribute("fafResults");
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
		AssignmentInfo info = (AssignmentInfo) s.getAttribute("buildAssign");
		if (!fList.isEmpty()) {
			// Get the first flight for the eq type
			Flight ff = fList.get(0);

			// Create the assignment if it doesn't exist
			if (info == null) {
				info = new AssignmentInfo(ff.getEquipmentType());
				info.setPilotID(ctx.getUser().getID());
				info.setStatus(AssignmentStatus.RESERVED);
				info.setRandom(true);
				info.setPurgeable(true);
				info.setAssignDate(Instant.now());
			}
			
			// Get equipment override
			String eqOv = ctx.getParameter("eqOverride");
			if ((eqOv != null) && (eqOv.length() < 3))
				eqOv = null;

			// Populate the legs
			for (Flight f : fList) {
				info.addAssignment(new AssignmentLeg(f));
				DraftFlightReport fr = new DraftFlightReport(f);
				fr.setDate(info.getAssignDate());
				if (eqOv != null) {
					fr.addStatusUpdate(info.getPilotID(), HistoryType.UPDATE, String.format("Equipment type substituted from %s to %s", f.getEquipmentType(), eqOv));
					fr.setEquipmentType(eqOv);
				}
				
				// Copy arrival/departure times
				if (f instanceof ScheduleEntry se) {
					fr.setTimeD(se.getTimeD().toLocalDateTime());
					fr.setTimeA(se.getTimeA().toLocalDateTime());
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
			
			// Get the equipment types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("allEQ", acdao.getAircraftTypes(), REQUEST);
			
			// If we have a leg, find the last one and update airline and src airport
			if ((info != null) && (criteria != null)) {
				DraftFlightReport lastLeg = info.getFlights().stream().filter(DraftFlightReport.class::isInstance).map(DraftFlightReport.class::cast).reduce((fst, snd) -> snd).orElse(null);
				if (lastLeg != null) {
					criteria.setAirline(lastLeg.getAirline());
					criteria.setAirportD(lastLeg.getAirportA());
					criteria.setHourA(-1);
					int h = (lastLeg.getTimeA() == null) ? -1 : lastLeg.getTimeA().getHour();
					criteria.setHourD((h > 22) ? -1 : (h+1));
				}
				
				results.clear();
			}
				
			// Get departure/arrival airports
			if (criteria != null) {
				GetScheduleAirport adao = new GetScheduleAirport(con);
				ctx.setAttribute("airports", adao.getOriginAirports(criteria.getAirline()), REQUEST);
				ctx.setAttribute("airportsA", adao.getConnectingAirports(criteria.getAirportD(), true, null), REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save airlines and combo variables for JSP
		List<Airline> airlines = SystemData.getAirlines().stream().filter(Airline::getActive).collect(Collectors.toList());
		ctx.setAttribute("airlines", airlines, REQUEST);
		ctx.setAttribute("sortTypes", ScheduleSearchCriteria.SORT_OPTIONS, REQUEST);
		ctx.setAttribute("hours", ScheduleSearchCriteria.HOURS, REQUEST);
		ctx.setAttribute("myEQ", ctx.getUser().getRatings(), REQUEST);

		// Redirect back to the find-a-flight page
		result.setURL("/jsp/schedule/findAflight.jsp");
		result.setSuccess(true);
	}
}