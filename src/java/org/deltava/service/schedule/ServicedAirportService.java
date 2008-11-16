// Copyright 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.schedule.*;
import org.deltava.comparators.AirportComparator;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to list all airports serviced by a particular Airline.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class ServicedAirportService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the airline
		Airline al = SystemData.getAirline(ctx.getParameter("airline"));
		if (al == null)
			throw error(SC_NOT_FOUND, "Unknown Airline - " + ctx.getParameter("airline"), false);

		Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.ICAO));
		try {
			GetScheduleAirport dao = new GetScheduleAirport(ctx.getConnection());
			airports.addAll(dao.getOriginAirports(al));
			airports.addAll(dao.getDestinationAirports(al));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Write the entries
		for (Iterator<Airport> i = airports.iterator(); i.hasNext(); ) {
			Airport a = i.next();
			Element e = new Element("airport");
			e.setAttribute("icao", a.getICAO());
			e.setAttribute("iata", a.getIATA());
			e.setAttribute("lat", StringUtils.format(a.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(a.getLongitude(), "##0.00000"));
			e.setAttribute("color", al.getColor());
			StringBuffer info = new StringBuffer(a.getInfoBox());
			info.append("<div class=\"mapInfoBox\"><br />Airlines:<br />");
			
			// Add Airlines
			for (Iterator<String> ai = a.getAirlineCodes().iterator(); ai.hasNext(); ) {
				Airline aal = SystemData.getAirline(ai.next());
				if (aal != null) {
					Element ae = new Element("airline");
					ae.setAttribute("name", aal.getName());
					ae.setAttribute("code", aal.getCode());
					e.addContent(ae);
					info.append(aal.getName());
					if (ai.hasNext())
						info.append("<br />");
				}
			}
			
			// Build info box
			info.append("</div>");
			e.addContent(new CDATA(info.toString()));
			
			// Add to results
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