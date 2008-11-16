// Copyright 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display scheduled routes out of a particular Airport. 
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class RouteService extends WebService {
	
	private static final Map<String, String> LCOLORS = CollectionUtils.createMap(Arrays.asList(MapEntry.COLORS),
			Arrays.asList(MapEntry.LINECOLORS));
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the airport
		Airport a = SystemData.getAirport(ctx.getParameter("icao"));
		if (a == null)
			throw error(SC_NOT_FOUND, "Unknown Airport - " + ctx.getParameter("icao"), false);
		
		Collection<ScheduleEntry> flights = null;
		try {
			GetSchedule dao = new GetSchedule(ctx.getConnection());
			flights = dao.getFlights(a);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Create the routes
		Collection<Airport> dstAirports = new TreeSet<Airport>();
		for (Iterator<ScheduleEntry> i = flights.iterator(); i.hasNext(); ) {
			ScheduleEntry entry = i.next();
			Airport ap = entry.getAirportA();
			if (!dstAirports.contains(ap)) {
				Collection<? extends GeoLocation> gc = Arrays.asList(a, ap); 
				Element gce = new Element("route");
				gce.setAttribute("from", a.getICAO());
				gce.setAttribute("to", ap.getICAO());
				gce.setAttribute("airline", entry.getAirline().getCode());
				gce.setAttribute("color", LCOLORS.get(entry.getAirline().getColor()));
				for (Iterator<? extends GeoLocation> gci = gc.iterator(); gci.hasNext(); ) {
					GeoLocation loc = gci.next();
					Element pe = new Element("pos");
					pe.setAttribute("lat", StringUtils.format(loc.getLatitude(), "##0.00000"));
					pe.setAttribute("lng", StringUtils.format(loc.getLongitude(), "##0.00000"));
					gce.addContent(pe);
				}

				// Add to the root element
				re.addContent(gce);
				dstAirports.add(ap);
			}
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