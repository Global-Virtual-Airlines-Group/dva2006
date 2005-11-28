// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;
import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;

import org.deltava.dao.GetSchedule;
import org.deltava.dao.DAOException;

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

	/**
	 * Helper method to parse a numeric request parameter.
	 */
	private int parse(String param) {
		if ("".equals(param))
			return 0;
		try {
			return Integer.parseInt(param);
		} catch (NumberFormatException nfe) {
			return 0;
		}
	}

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Set combo variables for JSP
		ctx.setAttribute("sortTypes", ComboUtils.fromArray(SORT_NAMES, SORT_OPTIONS), REQUEST);
		ctx.setAttribute("emptyList", Collections.EMPTY_LIST, REQUEST);
		
		// Get the airports
		Map<String, Airport> allAirports = SystemData.getAirports();
		Set<Airport> airports = new TreeSet<Airport>(new AirportComparator<Airport>(AirportComparator.NAME));
		airports.addAll(allAirports.values());
		ctx.setAttribute("airports", airports, REQUEST);

		// Get the result JSP and redirect if we're not posting
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/findAflight.jsp");
		if (ctx.getParameter("eqType") == null) {
			result.setSuccess(true);
			return;
		}

		// Populate the search criteria from the request
		Airline a = SystemData.getAirline(ctx.getParameter("airline"));
		ScheduleSearchCriteria criteria = new ScheduleSearchCriteria(a, parse(ctx.getParameter("flightNumber")),
				parse(ctx.getParameter("flightLeg")));
		criteria.setEquipmentType(ctx.getParameter("eqType"));
		criteria.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
		criteria.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
		criteria.setDistance(parse(ctx.getParameter("distance")));
		criteria.setMaxResults(parse(ctx.getParameter("maxResults")));
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

				// Get the DAO and execute
				GetSchedule dao = new GetSchedule(con);
				dao.setQueryMax(criteria.getMaxResults());

				// Save results in the session - since other commands may reference these
				ctx.setAttribute("fafResults", dao.search(criteria, sortType), SESSION);
				ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
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