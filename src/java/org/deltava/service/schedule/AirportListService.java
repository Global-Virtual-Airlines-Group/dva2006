// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.schedule.*;
import org.deltava.comparators.AirportComparator;

import org.deltava.dao.*;

import org.deltava.service.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to process Airport List AJAX requests.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class AirportListService extends WebService {

	private class NonFilter implements AirportFilter {

		protected NonFilter() {
			super();
		}

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
			return ((a == null) || (_a == null)) ? false : a.getAirlineCodes().contains(_a.getCode());
		}
	}

	private class AirportListFilter implements AirportFilter {

		private final Collection<String> _airportCodes = new HashSet<String>();

		AirportListFilter(Collection<Airport> airports) {
			super();
			for (Iterator<Airport> i = airports.iterator(); i.hasNext();) {
				Airport a = i.next();
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
		Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
		try {
			AirportFilter filter = null;
			Connection con = ctx.getConnection();

			String al = ctx.getParameter("airline");
			if (al != null) {
				boolean useSched = Boolean.valueOf(ctx.getParameter("useSched")).booleanValue();
				Airline a = SystemData.getAirline(al);

				// Either search the schedule or return the SystemData list
				if (useSched) {
					GetScheduleAirport dao = new GetScheduleAirport(con);
					filter = new AirportListFilter(dao.getOriginAirports(a));
				} else {
					if ("all".equalsIgnoreCase(al))
						filter = new NonFilter();
					else if ("charts".equalsIgnoreCase(al)) {
						GetChart dao = new GetChart(con);
						filter = new AirportListFilter(dao.getAirports());
					} else
						filter = new AirlineFilter(a);
				}
			} else if (ctx.getParameter("code") != null) {
				// Check if we are searching origin/departure
				boolean isDest = Boolean.valueOf(ctx.getParameter("dst")).booleanValue();
				Airport a = SystemData.getAirport(ctx.getParameter("code").toUpperCase());
				if (a == null)
					throw error(SC_BAD_REQUEST, "Invalid Airport", false);

				// Get the airports from the schedule database
				GetScheduleAirport dao = new GetScheduleAirport(con);
				filter = new AirportListFilter(dao.getConnectingAirports(a, !isDest, null));
			}
			
			// Generate the destination list
			GetAirport adao = new GetAirport(con);
			Map<String, Airport> allAirports = adao.getAll();
			for (Iterator<Airport> i = allAirports.values().iterator(); i.hasNext();) {
				Airport a = i.next();
				if (filter.accept(a))
					airports.add(a);
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);
		for (Iterator<Airport> i = airports.iterator(); i.hasNext(); ) {
			Airport a = i.next();
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
			ctx.getResponse().setContentType("text/xml");
			ctx.getResponse().setCharacterEncoding("UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
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