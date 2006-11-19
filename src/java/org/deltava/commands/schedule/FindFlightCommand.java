// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;

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

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Set combo variables for JSP
		ctx.setAttribute("sortTypes", ScheduleSearchCriteria.SORT_OPTIONS, REQUEST);
		ctx.setAttribute("hours", ScheduleSearchCriteria.HOURS, REQUEST);
		
		// Get the airline and the airports
		Airline a = SystemData.getAirline(ctx.getParameter("airline"));
		try {
			Connection con = ctx.getConnection();
			
			// Get the airports
			GetScheduleAirport adao = new GetScheduleAirport(con);
			Collection<Airport> airports = adao.getOriginAirports(a);
			ctx.setAttribute("airports", airports, REQUEST);
			if (ctx.getParameter("airline") == null)
				ctx.setAttribute("airportsA", adao.getDestinationAirports(null), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Get Airlines
		Collection<Airline> airlines = new LinkedHashSet<Airline>(SystemData.getAirlines().values());
		for (Iterator<Airline> i = airlines.iterator(); i.hasNext(); ) {
			Airline al = i.next();
			if (!al.getActive())
				i.remove();
		}
		
		// Save airlines
		ctx.setAttribute("airlines", airlines, REQUEST);
		
		// Get the result JSP and redirect if we're not posting
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/findAflight.jsp");
		if (ctx.getParameter("airline") == null) {
			result.setSuccess(true);
			return;
		}

		// Populate the search criteria from the request
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
		if (StringUtils.arrayIndexOf(ScheduleSearchCriteria.SORT_CODES, sortType) == -1)
			sortType = ScheduleSearchCriteria.SORT_CODES[0];

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
				
				// Save destination airport list
				GetScheduleAirport adao = new GetScheduleAirport(con);
				ctx.setAttribute("airportsA", adao.getConnectingAirports(criteria.getAirportD(), true, null), REQUEST);
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