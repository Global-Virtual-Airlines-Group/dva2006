// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;
import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to search the Flight Schedule.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FindFlightCommand extends AbstractCommand {

	private static final String[] SORT_NAMES = { "Random", "Flight Number", "Equipment Type", "Origin", 
		"Destination", "Departure Time", "Arrival Time", "Length", "Distance" }; 
	private static final String[] SORT_OPTIONS = { "RAND()", "FLIGHT", "EQTYPE", "AIRPORT_D", "AIRPORT_A",
		"TIME_D", "TIME_A", "FLIGHT_TIME", "DISTANCE"};
	
	private static final List HOURS = ComboUtils.fromArray(new String[] {"-", "Midnight", "1 AM", "2 AM", "3 AM", "4 AM", "5 AM", "6 AM",
			"7 AM", "8 AM", "9 AM", "10 AM", "11 AM", "Noon", "1 PM", "2 PM", "3 PM", "4 PM", "5 PM", "6 PM", "7 PM", "8 PM", "9 PM",
			"10 PM", "11 PM"}, new String[] {"-1", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
			"18", "19", "20", "21", "22", "23"});

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Set combo variables for JSP
		ctx.setAttribute("sortTypes", ComboUtils.fromArray(SORT_NAMES, SORT_OPTIONS), REQUEST);
		ctx.setAttribute("emptyList", Collections.EMPTY_LIST, REQUEST);
		ctx.setAttribute("hours", HOURS, REQUEST);
		
		// Get the result JSP and redirect if we're not posting
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/findAflight.jsp");
		if (ctx.getParameter("eqType") == null) {
			result.setSuccess(true);
			return;
		}

		// Populate the search criteria from the request
		Airline a = SystemData.getAirline(ctx.getParameter("airline"));
		ScheduleSearchCriteria criteria = new ScheduleSearchCriteria(a, StringUtils.parse(ctx.getParameter("flightNumber"), 0),
				StringUtils.parse(ctx.getParameter("flightLeg"), 0));
		criteria.setEquipmentType(ctx.getParameter("eqType"));
		criteria.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
		criteria.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
		criteria.setDistance(StringUtils.parse(ctx.getParameter("distance"), 0));
		criteria.setMaxResults(StringUtils.parse(ctx.getParameter("maxResults"), 0));
		criteria.setHourA(StringUtils.parse(ctx.getParameter("hourA"), -1));
		criteria.setHourD(StringUtils.parse(ctx.getParameter("hourD"), -1));
		if ((criteria.getMaxResults() < 1) || (criteria.getMaxResults() > 150))
			criteria.setMaxResults(100);
		
		// Validate sort criteria
		String sortType = ctx.getParameter("sortType"); 
		if (StringUtils.arrayIndexOf(SORT_OPTIONS, sortType) == -1)
			sortType = SORT_OPTIONS[0];

		// Save the search criteria in the session
		ctx.setAttribute("fafCriteria", criteria, SESSION);

		// Check if we're doing a new search or returning back existing criteria
		String opName = (String) ctx.getCmdParameter(OPERATION, "search");
		if (opName.equals("search")) {
			try {
				Connection con = ctx.getConnection();
				
				// Get the airports
				Set<Airport> airports = new TreeSet<Airport>(new AirportComparator<Airport>(AirportComparator.NAME));
				GetScheduleAirport adao = new GetScheduleAirport(con);
				airports.addAll(adao.getOriginAirports(a));

				// Get the DAO and execute
				GetSchedule dao = new GetSchedule(con);
				dao.setQueryMax(criteria.getMaxResults());

				// Save results in the session - since other commands may reference these
				ctx.setAttribute("fafResults", dao.search(criteria, sortType), SESSION);
				ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
				ctx.setAttribute("airports", airports, REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		}

		// Forward to the JSP
		result.setSuccess(true);
	}
}