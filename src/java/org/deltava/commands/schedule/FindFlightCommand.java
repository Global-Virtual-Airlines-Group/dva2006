// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to search the Flight Schedule.
 * @author Luke
 * @version 5.1
 * @since 1.0
 */

public class FindFlightCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	@Override
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
		for (Iterator<Airline> i = airlines.iterator(); i.hasNext(); ) {
			Airline al = i.next();
			if (!al.getActive())
				i.remove();
		}
		
		// Save airlines and ratings
		ctx.setAttribute("airlines", airlines, REQUEST);
		ctx.setAttribute("myEQ", ctx.getUser().getRatings(), REQUEST);
		
		// Get the result JSP and redirect if we're not posting
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/findAflight.jsp");
		if (ctx.getParameter("airline") == null) {
			result.setSuccess(true);
			return;
		}

		// Populate the search criteria from the request
		int leg = Math.min(Math.max(0, StringUtils.parse(ctx.getParameter("flightLeg"), 0)), 8);
		ScheduleSearchCriteria criteria = new ScheduleSearchCriteria(a, StringUtils.parse(ctx.getParameter("flightNumber"), 0), leg);
		criteria.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
		criteria.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
		criteria.setDistance(StringUtils.parse(ctx.getParameter("distance"), 0));
		criteria.setDistanceRange(StringUtils.parse(ctx.getParameter("distRange"), 150));
		criteria.setMaxResults(StringUtils.parse(ctx.getParameter("maxResults"), 0));
		criteria.setHourA(StringUtils.parse(ctx.getParameter("hourA"), -1));
		criteria.setHourD(StringUtils.parse(ctx.getParameter("hourD"), -1));
		criteria.setDBName(SystemData.get("airline.db"));
		criteria.setCheckDispatchRoutes(Boolean.valueOf(ctx.getParameter("checkDispatch")).booleanValue());
		criteria.setDispatchOnly(Boolean.valueOf(ctx.getParameter("dispatchOnly")).booleanValue());
		criteria.setFlightsPerRoute(StringUtils.parse(ctx.getParameter("maxFlights"), 0));
		criteria.setIncludeAcademy(ctx.isUserInRole("Instructor") || ctx.isUserInRole("Schedule") || ctx.isUserInRole("HR") || ctx.isUserInRole("Operations"));
		if ((criteria.getMaxResults() < 1) || (criteria.getMaxResults() > 150))
			criteria.setMaxResults(150);
		
		// Set equipment type(s)
		boolean myRatedEQ = Boolean.valueOf(ctx.getParameter("myEQTypes")).booleanValue();
		if (myRatedEQ)
			criteria.setEquipmentTypes(ctx.getUser().getRatings());
		else
			criteria.setEquipmentType(ctx.getParameter("eqType"));
		
		// Validate sort criteria
		String sortType = ctx.getParameter("sortType"); 
		if (StringUtils.arrayIndexOf(ScheduleSearchCriteria.SORT_CODES, sortType) == -1)
			sortType = ScheduleSearchCriteria.SORT_CODES[0];
		
		// Check for descending sort
		boolean isDesc = Boolean.valueOf(ctx.getParameter("sortDesc")).booleanValue();
		if (isDesc)
			sortType += " DESC";

		// Save the search criteria in the session
		criteria.setSortBy(sortType);
		ctx.setAttribute("fafCriteria", criteria, SESSION);

		// Check if we're doing a new search or returning back existing criteria
		String opName = (String) ctx.getCmdParameter(OPERATION, "search");
		if (opName.equals("search")) {
			try {
				Connection con = ctx.getConnection();
				
				// Get the DAO and execute
				GetScheduleSearch dao = new GetScheduleSearch(con);
				dao.setQueryMax(criteria.getMaxResults());
				ScheduleSearchResults results = new ScheduleSearchResults(dao.search(criteria));
				
		         // Load schedule import data
		    	 GetMetadata mddao = new GetMetadata(con);
		    	 String lastImport = mddao.get(SystemData.get("airline.code").toLowerCase() + ".schedule.import");
		    	 if (lastImport != null)
		    		 results.setImportDate(new Date(Long.parseLong(lastImport) * 1000));

				// Save results in the session - since other commands may reference these
				ctx.setAttribute("fafResults", results, SESSION);
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
		
		// Save UTC time zone and forward
		ctx.setAttribute("utc", TZInfo.UTC, REQUEST);
		result.setSuccess(true);
	}
}