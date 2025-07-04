// Copyright 2008, 2009, 2010, 2011, 2012, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.time.Instant;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.Inclusion;
import org.deltava.beans.assign.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to search the schedule to build a flight assignment that consists of a single leg selected at
 * random from the last Airport the Pilot completed a flight to in the selected aircraft.
 * @author Luke
 * @version 11.2
 * @since 2.2
 */

public class SingleAssignmentSearchCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the result
		CommandResult result = ctx.getResult();
		
		// Build the search criteria
		ScheduleSearchCriteria criteria = new ScheduleSearchCriteria("RAND()");
		criteria.setDBName(ctx.getDB());
		criteria.setPilotID(ctx.getUser().getID());
		criteria.setDistance(StringUtils.parse(ctx.getParameter("maxLength"), 0));
		criteria.setDistanceRange(Math.max((criteria.getDistance() == 0) ? 0 : 150, StringUtils.parse(ctx.getParameter("maxLengthRange"), 0)));
		criteria.setNotVisitedA(Boolean.parseBoolean(ctx.getParameter("avoidVisitedDestination")));
		criteria.setExcludeHistoric(EnumUtils.parse(Inclusion.class, ctx.getParameter("avoidHistorical"), Inclusion.ALL));

		// Get total legs to load
		int totalLegs = Math.min(8, Math.max(1, StringUtils.parse(ctx.getParameter("legs"), 1)));
		try {
			Connection con = ctx.getConnection();

			// Check if we have any other open flight assignments, if so abort
			GetAssignment dao = new GetAssignment(con);
			List<AssignmentInfo> assignments = dao.getByPilot(ctx.getUser().getID(), AssignmentStatus.RESERVED);
			if (!assignments.isEmpty()) {
				ctx.release();
				result.setURL("/jsp/assign/assignOpen.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Get the last airport we flew into
			GetFlightReports prdao = new GetFlightReports(con);
			prdao.setQueryMax(10);
			List<FlightReport> pireps = prdao.getByPilot(ctx.getUser().getID(), new LogbookSearchCriteria("SUBMITTED DESC", ctx.getDB()));
			Optional<FlightReport> ofr = pireps.stream().filter(fr -> ((fr.getStatus() == FlightStatus.OK) || (fr.getStatus() == FlightStatus.SUBMITTED))).findFirst();
			if (!ofr.isPresent())
				throw notFoundException("No flights logged");
			
			// Save the user
			ctx.setAttribute("pilot", ctx.getUser(), REQUEST);
			ctx.setAttribute("airlines", SystemData.getAirlines(), REQUEST);
			
			// Get the equipment families
			GetAircraft acdao = new GetAircraft(con);
			Collection<Aircraft> allEQ = acdao.getAircraftTypes().stream().filter(ac -> !ac.getAcademyOnly()).collect(Collectors.toList());
			ctx.setAttribute("allFamily", allEQ.stream().map(Aircraft::getFamily).filter(Objects::nonNull).collect(Collectors.toCollection(TreeSet::new)), REQUEST);

			// Get additional parameters if we are redoing a search
			final String f = ctx.getParameter("family");
			criteria.setAirportD(ofr.get().getAirportA());
			criteria.setAirline(SystemData.getAirline(ctx.getParameter("airline")));
			if (!StringUtils.isEmpty(f) && !"-".equals(f)) {
				criteria.setEquipmentTypes(allEQ.stream().filter(ac -> f.equalsIgnoreCase(ac.getFamily())).map(Aircraft::getName).collect(Collectors.toSet()));
				ctx.setAttribute("eqFamily", f, REQUEST);
			} else if (ctx.getParameter("eqType") != null)
				criteria.setEquipmentType(ctx.getParameter("eqType"));
			
			// Define Variables
			int oldDistance = criteria.getDistance();
			AssignmentInfo ai = null; Airport startAirport = criteria.getAirportD();
			Collection<ScheduleEntry> flightEntries = new ArrayList<ScheduleEntry>();
			
			// Load the schedule entries - allow multiple legs
			GetRawSchedule rsdao = new GetRawSchedule(con);
			GetScheduleSearch sdao = new GetScheduleSearch(con);
			sdao.setSources(rsdao.getSources(true, ctx.getDB()));
			sdao.setQueryMax(1);
			for (int x = 0; x < totalLegs; x++) {
				List<ScheduleEntry> legs = sdao.search(criteria);	
				if (ai == null) {
					String eqType = legs.isEmpty() ? "RANDOM" : legs.get(0).getEquipmentType();
					ai = new AssignmentInfo(eqType);
					ai.setAssignDate(Instant.now());
				}
				
				// Re-search if a distance query was used
				if (legs.isEmpty() && (criteria.getDistance()  > 0)) {
					criteria.setDistance(0);
					legs = sdao.search(criteria);
					criteria.setDistance(oldDistance);
				}
				
				// Add the leg
				if (!legs.isEmpty()) {
					ScheduleEntry entry = legs.get(0);
					ai.addAssignment(new AssignmentLeg(entry));
					DraftFlightReport fr = new DraftFlightReport(entry);
					fr.setDate(ai.getAssignDate());
					fr.setTimeD(entry.getTimeD().toLocalDateTime());
					fr.setTimeA(entry.getTimeA().toLocalDateTime());
					fr.setAttribute(FlightReport.ATTR_HISTORIC, entry.getHistoric());
					fr.setRemarks(fr.getDraftComments());
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