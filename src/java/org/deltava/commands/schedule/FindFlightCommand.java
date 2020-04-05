// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2015, 2016, 2017, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.Inclusion;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.filter.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to search the Flight Schedule.
 * @author Luke
 * @version 9.0
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

		// Save airlines and ratings
		List<Airline> airlines = SystemData.getAirlines().values().stream().filter(Airline::getActive).collect(Collectors.toList());
		Collections.sort(airlines);
		ctx.setAttribute("airlines", airlines, REQUEST);
		ctx.setAttribute("myEQ", ctx.getUser().getRatings(), REQUEST);
		ctx.setAttribute("airline", SystemData.getAirline(SystemData.get("airline.code")), REQUEST);

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
		criteria.setExcludeHistoric((a != null) ? Inclusion.ALL : EnumUtils.parse(Inclusion.class, ctx.getParameter("historicOnly"), Inclusion.ALL));
		criteria.setDispatchOnly(EnumUtils.parse(Inclusion.class, ctx.getParameter("dispatchOnly"), Inclusion.ALL));
		criteria.setFlightsPerRoute(StringUtils.parse(ctx.getParameter("maxFlights"), 0));
		criteria.setIncludeAcademy(ctx.isUserInRole("Instructor") || ctx.isUserInRole("Schedule") || ctx.isUserInRole("HR") || ctx.isUserInRole("Operations") ? Inclusion.ALL : Inclusion.EXCLUDE);
		criteria.setPilotID(ctx.getUser().getID());
		criteria.setLastFlownInterval(StringUtils.parse(ctx.getParameter("maxLastFlown"), -1));
		criteria.setRouteLegs(StringUtils.parse(ctx.getParameter("maxRouteLegs"), -1));
		criteria.setNotVisitedD(Boolean.valueOf(ctx.getParameter("nVD")).booleanValue());
		criteria.setNotVisitedA(Boolean.valueOf(ctx.getParameter("nVA")).booleanValue());
		if ((criteria.getMaxResults() < 1) || (criteria.getMaxResults() > 150))
			criteria.setMaxResults(150);

		// Set equipment type(s)
		if (Boolean.valueOf(ctx.getParameter("myEQTypes")).booleanValue())
			criteria.setEquipmentTypes(ctx.getUser().getRatings());
		else
			criteria.setEquipmentType(ctx.getParameter("eqType"));

		// Validate sort criteria
		String sortType = ctx.getParameter("sortType");
		if (StringUtils.arrayIndexOf(ScheduleSearchCriteria.SORT_CODES, sortType) == -1)
			sortType = ScheduleSearchCriteria.SORT_CODES[0];

		// Check for descending sort
		if (Boolean.valueOf(ctx.getParameter("sortDesc")).booleanValue())
			sortType += " DESC";

		// Save the search criteria in the session
		criteria.setSortBy(sortType);
		ctx.setAttribute("fafCriteria", criteria, SESSION);

		// Check if we're doing a new search or returning back existing criteria
		String opName = (String) ctx.getCmdParameter(OPERATION, "search");
		if ("search".equals(opName)) {
			try {
				Connection con = ctx.getConnection();

				// Load schedule import metadata
				GetRawSchedule rsdao = new GetRawSchedule(con);
				Collection<ScheduleSourceInfo> srcs = rsdao.getSources(true);

				// Get the DAO and execute
				GetScheduleSearch dao = new GetScheduleSearch(con);
				dao.setQueryMax(criteria.getMaxResults());
				dao.setSources(srcs);

				// Get destination airports
				GetScheduleAirport adao = new GetScheduleAirport(con);
				Collection<Airport> dsts = adao.getConnectingAirports(criteria.getAirportD(), true, criteria.getAirline());
				if (criteria.getNotVisitedA()) {
					GetFlightReports frdao = new GetFlightReports(con);
					Collection<? extends RoutePair> routes = frdao.getRoutePairs(ctx.getUser().getID(), 0);
					Collection<Airport> myAirports = routes.stream().flatMap(rp -> List.of(rp.getAirportD(), rp.getAirportA()).stream()).collect(Collectors.toCollection(LinkedHashSet::new));
					AirportFilter fl = new NOTFilter(new IATAFilter(myAirports));
					ctx.setAttribute("airportsA", fl.filter(dsts), REQUEST);
				} else
					ctx.setAttribute("airportsA", dsts, REQUEST);

				// Save results in the session - since other commands may reference these
				Collection<ScheduleEntry> results = dao.search(criteria);
				Collection<ScheduleSource> resultSources = results.stream().map(ScheduleEntry::getSource).collect(Collectors.toSet());  
				ctx.setAttribute("scheduleSources", srcs.stream().filter(srcInfo -> resultSources.contains(srcInfo.getSource())).collect(Collectors.toSet()), REQUEST);
				ctx.setAttribute("fafResults", results, SESSION);
				ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		}

		result.setSuccess(true);
	}
}