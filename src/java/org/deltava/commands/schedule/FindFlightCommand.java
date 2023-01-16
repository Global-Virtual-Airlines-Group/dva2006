// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2015, 2016, 2017, 2019, 2020, 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.Inclusion;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.filter.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to search the Flight Schedule.
 * @author Luke
 * @version 10.4
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
		
		// Init search criteria
		ScheduleSearchCriteria ssc = (ScheduleSearchCriteria) ctx.getSession().getAttribute("fafCriteria");
		boolean hasCriteria = (ssc != null);
		boolean isSearch = Boolean.parseBoolean(ctx.getParameter("doSearch"));
		Airline a = SystemData.getAirline(ctx.getParameter("airline"));
		int fn = Math.max(0, StringUtils.parse(ctx.getParameter("flightNumber"), 0));
		int leg = Math.min(Math.max(0, StringUtils.parse(ctx.getParameter("flightLeg"), 0)), 8);
		if (ssc != null) {
			ssc.setAirline(a);
			ssc.setFlightNumber(fn);
			ssc.setLeg(leg);
		} else
			ssc = new ScheduleSearchCriteria(a, fn, leg);
		
		Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
		if (aD != null)
			ssc.setAirportD(aD);

		// Get the airline and the airports
		Collection<Aircraft> allEQ = new ArrayList<Aircraft>();
		try {
			Connection con = ctx.getConnection();

			// Get the airports
			GetScheduleAirport adao = new GetScheduleAirport(con);
			Collection<Airport> airports = adao.getOriginAirports(a);
			ctx.setAttribute("airports", airports, REQUEST);
			
			// Load recent PIREPs to see if we have a connected logbook
			if (!isSearch) {
				GetFlightReports frdao = new GetFlightReports(con);
				frdao.setQueryMax(10);
				List<FlightReport> pireps = frdao.getByPilot(ctx.getUser().getID(), new LogbookSearchCriteria("DATE DESC, PR.SUBMITTED DESC, PR.ID DESC", ctx.getDB()));
				if (!hasCriteria && (pireps.size() > 2) && (ssc.getAirportD() == null)) {
					LogbookHistoryHelper lh = new LogbookHistoryHelper(pireps);
					if (lh.isContinuous(3)) {
						ssc.setAirportD(lh.getLastFlight().getAirportA());
						ssc.setMaxResults(25);
						ctx.setAttribute("fafCriteria", ssc, SESSION);
						ctx.setAttribute("airportsA", adao.getConnectingAirports(ssc.getAirportD(), true, a), REQUEST);
					}
					
					if (lh.isCurent(5))
						ssc.setExcludeHistoric(Inclusion.EXCLUDE);
					else if (lh.isHistoric(5))
						ssc.setExcludeHistoric(Inclusion.INCLUDE);
				}
			} else if (ctx.getParameter("airline") == null)
				ctx.setAttribute("airportsA", adao.getDestinationAirports(null), REQUEST);

			// Get the equipment types
			GetAircraft acdao = new GetAircraft(con);
			acdao.getAircraftTypes().stream().filter(ac -> !ac.getAcademyOnly()).forEach(allEQ::add);
			ctx.setAttribute("allEQ", allEQ, REQUEST);
			ctx.setAttribute("allFamily", allEQ.stream().map(Aircraft::getFamily).filter(Objects::nonNull).collect(Collectors.toCollection(TreeSet::new)), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save airlines and ratings
		List<Airline> airlines = SystemData.getAirlines().stream().filter(Airline::getActive).collect(Collectors.toList());
		ctx.setAttribute("airlines", airlines, REQUEST);
		ctx.setAttribute("myEQ", ctx.getUser().getRatings(), REQUEST);
		ctx.setAttribute("airline", SystemData.getAirline(SystemData.get("airline.code")), REQUEST);

		// Get the result JSP and redirect if we're not posting
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/findAflight.jsp");
		if (!isSearch) {
			result.setSuccess(true);
			return;
		}

		// Populate the search criteria from the request
		ssc.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
		ssc.setDistance(StringUtils.parse(ctx.getParameter("distance"), 0));
		ssc.setDistanceRange(StringUtils.parse(ctx.getParameter("distRange"), 150));
		ssc.setMaxResults(StringUtils.parse(ctx.getParameter("maxResults"), 0));
		ssc.setHourA(StringUtils.parse(ctx.getParameter("hourA"), -1));
		ssc.setHourD(StringUtils.parse(ctx.getParameter("hourD"), -1));
		ssc.setDBName(ctx.getDB());
		ssc.setCheckDispatchRoutes(Boolean.parseBoolean(ctx.getParameter("checkDispatch")));
		ssc.setExcludeHistoric((a != null) ? Inclusion.ALL : EnumUtils.parse(Inclusion.class, ctx.getParameter("historicOnly"), Inclusion.ALL));
		ssc.setDispatchOnly(EnumUtils.parse(Inclusion.class, ctx.getParameter("dispatchOnly"), Inclusion.ALL));
		ssc.setFlightsPerRoute(StringUtils.parse(ctx.getParameter("maxFlights"), 0));
		ssc.setIncludeAcademy(ctx.isUserInRole("Instructor") || ctx.isUserInRole("Schedule") || ctx.isUserInRole("HR") || ctx.isUserInRole("Operations") ? Inclusion.ALL : Inclusion.EXCLUDE);
		ssc.setPilotID(ctx.getUser().getID());
		ssc.setLastFlownInterval(StringUtils.parse(ctx.getParameter("maxLastFlown"), -1));
		ssc.setRouteLegs(StringUtils.parse(ctx.getParameter("maxRouteLegs"), -1));
		ssc.setNotVisitedD(Boolean.parseBoolean(ctx.getParameter("nVD")));
		ssc.setNotVisitedA(Boolean.parseBoolean(ctx.getParameter("nVA")));
		ssc.setMaxResults(Math.max(0, Math.min(ssc.getMaxResults(), 500)));

		// Set equipment type(s)
		final String f = ctx.getParameter("family");
		if (Boolean.parseBoolean(ctx.getParameter("myEQTypes")))
			ssc.setEquipmentTypes(ctx.getUser().getRatings());
		else if (!StringUtils.isEmpty(f) && !"-".equals(f)) {
			ssc.setEquipmentTypes(allEQ.stream().filter(ac -> f.equalsIgnoreCase(ac.getFamily())).map(Aircraft::getName).collect(Collectors.toSet()));
			ctx.setAttribute("eqFamily", f, REQUEST);
		} else
			ssc.setEquipmentType(ctx.getParameter("eqType"));

		// Validate sort criteria
		String sortType = ctx.getParameter("sortType");
		if (StringUtils.arrayIndexOf(ScheduleSearchCriteria.SORT_CODES, sortType) == -1)
			sortType = ScheduleSearchCriteria.SORT_CODES[0];

		// Check for descending sort
		if (Boolean.parseBoolean(ctx.getParameter("sortDesc")))
			sortType += " DESC";

		// Save the search criteria in the session
		ssc.setSortBy(sortType);
		ctx.setAttribute("fafCriteria", ssc, SESSION);

		// Check if we're doing a new search or returning back existing criteria
		String opName = (String) ctx.getCmdParameter(OPERATION, "search");
		if ("search".equals(opName)) {
			try {
				Connection con = ctx.getConnection();

				// Load schedule import metadata
				GetRawSchedule rsdao = new GetRawSchedule(con);
				Collection<ScheduleSourceInfo> srcs = rsdao.getSources(true, ctx.getDB());

				// Get the DAO and execute
				GetScheduleSearch dao = new GetScheduleSearch(con);
				dao.setQueryMax(ssc.getMaxResults());
				dao.setSources(srcs);

				// Get destination airports
				GetScheduleAirport adao = new GetScheduleAirport(con);
				Collection<Airport> dsts = adao.getConnectingAirports(ssc.getAirportD(), true, ssc.getAirline());
				if (ssc.getNotVisitedA()) {
					GetFlightReports frdao = new GetFlightReports(con);
					Collection<? extends RoutePair> routes = frdao.getRoutePairs(ctx.getUser().getID(), 0);
					Collection<Airport> myAirports = routes.stream().flatMap(rp -> rp.getAirports().stream()).collect(Collectors.toCollection(LinkedHashSet::new));
					AirportFilter fl = new NOTFilter(new IATAFilter(myAirports));
					ctx.setAttribute("airportsA", fl.filter(dsts), REQUEST);
				} else
					ctx.setAttribute("airportsA", dsts, REQUEST);

				// Save results in the session - since other commands may reference these
				Collection<ScheduleEntry> results = dao.search(ssc);
				Collection<ScheduleSource> resultSources = results.stream().map(ScheduleEntry::getSource).collect(Collectors.toSet());  
				ctx.setAttribute("scheduleSources", srcs.stream().filter(srcInfo -> resultSources.contains(srcInfo.getSource())).collect(Collectors.toMap(ScheduleSourceInfo::getSource, Function.identity())), REQUEST);
				ctx.setAttribute("fafResults", results, SESSION);
				ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
				ctx.setAttribute("hasLastFlight", Boolean.valueOf(results.stream().allMatch(ScheduleSearchEntry.class::isInstance)), REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		}

		result.setSuccess(true);
	}
}