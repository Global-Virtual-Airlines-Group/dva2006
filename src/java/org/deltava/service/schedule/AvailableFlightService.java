// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.dao.*;
import org.deltava.service.ServiceContext;
import org.deltava.service.ServiceException;
import org.deltava.service.WebDataService;
import org.deltava.util.*;

/**
 * A Web Service to return the next available Flight Number in the Flight Schedule.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AvailableFlightService extends WebDataService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get start/end ranges
		int startFlight = StringUtils.parse(ctx.getParameter("start"), 0);
		int endFlight = StringUtils.parse(ctx.getParameter("end"), 0);
		
		Collection<Integer> flights = null;
		try {
			GetScheduleInfo dao = new GetScheduleInfo(_con);
			flights = dao.getFlightNumbers(startFlight, endFlight);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}
		
		// Find the first available flight number in the range
		int flightNumber = startFlight + 1;
		for (Iterator<Integer> i = flights.iterator(); i.hasNext(); ) {
			Integer fn = i.next();
			if (flightNumber < fn.intValue())
				break;
			
			flightNumber++;
		}
			
		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);
		
		// Save the flight number
		re.setAttribute("number", String.valueOf(flightNumber));
		re.setAttribute("leg", "1");
		
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
}