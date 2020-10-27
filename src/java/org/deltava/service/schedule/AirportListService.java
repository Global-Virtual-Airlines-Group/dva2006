// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.schedule.*;
import org.deltava.comparators.AirportComparator;

import org.deltava.dao.*;
import org.deltava.filter.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to process Airport List AJAX requests.
 * @author Luke
 * @author Rahul
 * @version 8.7
 * @since 1.0
 */

public class AirportListService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		MultiFilter filter = new ANDFilter();
		filter.add(new NonFilter());

		// Figure out what kind of search we are doing
		Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
		Map<String, Airport> allAirports = new HashMap<String, Airport>();
		try {
			Connection con = ctx.getConnection();
			
			// Check for not visited airports
			if (Boolean.valueOf(ctx.getParameter("notVisited")).booleanValue() && ctx.isAuthenticated()) {
				GetFlightReports frdao = new GetFlightReports(con);
				Collection<? extends RoutePair> routes = frdao.getRoutePairs(ctx.getUser().getID(), 0);
				Collection<Airport> myAirports = routes.stream().flatMap(rp -> List.of(rp.getAirportD(), rp.getAirportA()).stream()).collect(Collectors.toCollection(LinkedHashSet::new));
				
				// Add academy airports
				GetSchedule sdao = new GetSchedule(con);
				myAirports.addAll(sdao.getAcademyAirports());
				
				// Create the filter
				filter.add(new NOTFilter(new IATAFilter(myAirports)));
			}
			
			String al = ctx.getParameter("airline");
			boolean useSched = Boolean.valueOf(ctx.getParameter("useSched")).booleanValue();
			if (al != null) {
				Airline a = SystemData.getAirline(al);
				// Either search the schedule or return the SystemData list
				if (useSched) {
					boolean isDest = Boolean.valueOf(ctx.getParameter("dst")).booleanValue();
					GetScheduleAirport dao = new GetScheduleAirport(con);
					filter.add(new IATAFilter(isDest ? dao.getDestinationAirports(a) : dao.getOriginAirports(a)));
				} else {
					if ("charts".equalsIgnoreCase(al)) {
						GetChart dao = new GetChart(con);
						filter.add(new IATAFilter(dao.getAirports()));
					} else if (a != null)
						filter.add(new AirlineFilter(a));
				}
			} 
			
			// If we've specified a source airport filter that too
			if (ctx.getParameter("code") != null) {
				// Check if we are searching origin/departure
				boolean isDest = Boolean.valueOf(ctx.getParameter("dst")).booleanValue();
				Airport a = SystemData.getAirport(ctx.getParameter("code"));
				if (a == null)
					throw error(SC_BAD_REQUEST, "Invalid Airport", false);

				// Get the airports from the schedule database
				GetScheduleAirport dao = new GetScheduleAirport(con);
				filter.add(new IATAFilter(dao.getConnectingAirports(a, !isDest, SystemData.getAirline(al))));
			} else if (useSched) {
				GetScheduleAirport dao = new GetScheduleAirport(con);
				Collection<Airport> schedAirports = new LinkedHashSet<Airport>();
				schedAirports.addAll(dao.getOriginAirports(null));
				schedAirports.addAll(dao.getDestinationAirports(null));
				filter.add(new IATAFilter(schedAirports));
			}
			
			// Add supplementary country filter
			if (ctx.getParameter("country") != null) {
				Country c = Country.get(ctx.getParameter("country"));
				if (c == null)
					throw error(SC_BAD_REQUEST, "Invalid Country", false);
			
				filter.add(new CountryFilter(c));
			}
			
			// Add suplementary range filter
			if (ctx.getParameter("airport") != null) {
				Airport a = SystemData.getAirport(ctx.getParameter("airport"));
				if (a != null) {
					filter.add(new GeoLocationFilter(a, StringUtils.parse(ctx.getParameter("dist"), 5)));
					filter.add(new NOTFilter(new IATAFilter(a)));
				}
			}
			
			// Add supplementary runway lenght filter
			if (ctx.getParameter("eqType") != null) {
				GetAircraft acdao = new GetAircraft(con);
				Aircraft ac = acdao.get(ctx.getParameter("eqType"));
				if (ac != null) {
					AircraftPolicyOptions opts = ac.getOptions(SystemData.get("airline.code"));
					int rwyLength = Math.max(opts.getTakeoffRunwayLength(), opts.getLandingRunwayLength());
					filter.add(new RunwayLengthFilter(rwyLength));
				}
			}
			
			// Add forced airport
			GetAirport adao = new GetAirport(con);
			allAirports.putAll(adao.getAll());
			if (!StringUtils.isEmpty(ctx.getParameter("add")))
				airports.add(adao.get(ctx.getParameter("add")));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Do the filtering
		for (Airport a : allAirports.values()) {
			if (filter.accept(a)) {
				airports.add(a);
				if ((a.getSupercededAirport() != null) && allAirports.containsKey(a.getSupercededAirport()))
					airports.add(allAirports.get(a.getSupercededAirport()));
			}
		}

		// Generate the JSON document
		JSONArray ja = new JSONArray();
		for (Airport a : airports) {
			JSONObject ao = new JSONObject();
			ao.put("iata", a.getIATA());
			ao.put("icao", a.getICAO());
			ao.put("lat", a.getLatitude());
			ao.put("lng", a.getLongitude());
			ao.put("name", a.getName());
			ja.put(ao);
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(1800);
			ctx.println(ja.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	@Override
	public boolean isLogged() {
		return false;
	}
}