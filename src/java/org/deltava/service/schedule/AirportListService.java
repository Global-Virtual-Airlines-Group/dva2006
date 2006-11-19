// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.schedule.*;
import org.deltava.comparators.AirportComparator;

import org.deltava.dao.*;

import org.deltava.service.ServiceContext;
import org.deltava.service.ServiceException;
import org.deltava.service.WebDataService;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to process Airport List AJAX requests.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirportListService extends WebDataService {

	private class NonFilter implements AirportFilter {

		public boolean accept(Airport a) {
			return true;
		}
	}

	private class AirlineFilter implements AirportFilter {

		private Airline _a;

		AirlineFilter(Airline a) {
			super();
			_a = (a == null) ? SystemData.getAirline(SystemData.get("airline.code")) : a;
		}

		public boolean accept(Airport a) {
			return (a == null) ? false : a.getAirlineCodes().contains(_a.getCode());
		}
	}

	private class AirportListFilter implements AirportFilter {

		private Collection<String> _airportCodes;

		AirportListFilter(Collection airports) {
			super();
			_airportCodes = new HashSet<String>();
			for (Iterator i = airports.iterator(); i.hasNext();) {
				Airport a = (Airport) i.next();
				_airportCodes.add(a.getIATA());
			}
		}

		public boolean accept(Airport a) {
			return (a == null) ? false : _airportCodes.contains(a.getIATA());
		}
	}

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Figure out what kind of search we are doing
		AirportFilter filter = null;
		if (ctx.getParameter("airline") != null) {
			boolean useSched = Boolean.valueOf(ctx.getParameter("useSched")).booleanValue();
			Airline a = SystemData.getAirline(ctx.getParameter("airline"));

			// Either search the schedule or return the SystemData list
			if (useSched) {
				try {
					GetScheduleAirport dao = new GetScheduleAirport(_con);
					filter = new AirportListFilter(dao.getOriginAirports(a));
				} catch (DAOException de) {
					throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
				}
			} else {
				if ("all".equals(ctx.getParameter("airline"))) {
					filter = new NonFilter();
				} else {
					filter = new AirlineFilter(a);
				}
			}
		} else if (ctx.getParameter("code") != null) {
			// Check if we are searching origin/departure
			boolean isDest = Boolean.valueOf(ctx.getParameter("dst")).booleanValue();
			Airport a = SystemData.getAirport(ctx.getParameter("code").toUpperCase());
			if (a == null)
				throw error(SC_BAD_REQUEST, "Invalid Airport");

			// Get the airports from the schedule database
			try {
				GetScheduleAirport dao = new GetScheduleAirport(_con);
				filter = new AirportListFilter(dao.getConnectingAirports(a, !isDest, null));
			} catch (DAOException de) {
				throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
			}
		}

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Generate the destination list
		Map<String, Airport> allAirports = SystemData.getAirports();
		Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator<Airport>(AirportComparator.NAME));
		airports.addAll(allAirports.values());
		for (Iterator i = airports.iterator(); i.hasNext();) {
			Airport a = (Airport) i.next();
			if (filter.accept(a)) {
				Element e = new Element("airport");
				e.setAttribute("iata", a.getIATA());
				e.setAttribute("icao", a.getICAO());
				e.setAttribute("lat", StringUtils.format(a.getLatitude(), "##0.0000"));
				e.setAttribute("lng", StringUtils.format(a.getLongitude(), "##0.0000"));
				e.setAttribute("name", a.getName());
				re.addContent(e);
			}
		}

		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error");
		}

		// Return success code
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