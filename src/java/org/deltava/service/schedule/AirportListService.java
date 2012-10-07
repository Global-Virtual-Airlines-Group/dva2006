// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom2.*;

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
 * @version 5.0
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

			String al = ctx.getParameter("airline");
			boolean useSched = Boolean.valueOf(ctx.getParameter("useSched")).booleanValue();
			if (al != null) {
				Airline a = SystemData.getAirline(al);

				// Either search the schedule or return the SystemData list
				if (useSched) {
					GetScheduleAirport dao = new GetScheduleAirport(con);
					filter.add(new IATAFilter(dao.getOriginAirports(a)));
				} else {
					if ("charts".equalsIgnoreCase(al)) {
						GetChart dao = new GetChart(con);
						filter.add(new IATAFilter(dao.getAirports()));
					} else if (a != null)
						filter.add(new AirlineFilter(a));
				}
			} else if (ctx.getParameter("code") != null) {
				// Check if we are searching origin/departure
				boolean isDest = Boolean.valueOf(ctx.getParameter("dst")).booleanValue();
				Airport a = SystemData.getAirport(ctx.getParameter("code").toUpperCase());
				if (a == null)
					throw error(SC_BAD_REQUEST, "Invalid Airport", false);

				// Get the airports from the schedule database
				GetScheduleAirport dao = new GetScheduleAirport(con);
				filter.add(new IATAFilter(dao.getConnectingAirports(a, !isDest, null)));
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
				if (a != null)
					filter.add(new GeoLocationFilter(a, StringUtils.parse(ctx.getParameter("dist"), 5)));
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

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);
		for (Airport a : airports) {
			Element e = new Element("airport");
			e.setAttribute("iata", a.getIATA());
			e.setAttribute("icao", a.getICAO());
			e.setAttribute("lat", StringUtils.format(a.getLatitude(), "##0.0000"));
			e.setAttribute("lng", StringUtils.format(a.getLongitude(), "##0.0000"));
			e.setAttribute("name", a.getName());
			re.addContent(e);
		}
		
		// Dump the XML to the output stream
		try {
			ctx.setExpires(3600);
			ctx.setContentType("text/xml", "UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
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
	public final boolean isLogged() {
		return false;
	}
}