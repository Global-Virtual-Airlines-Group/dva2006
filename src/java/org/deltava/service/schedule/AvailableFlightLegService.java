// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.schedule.Airline;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to return the next available Leg number for a Flight.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AvailableFlightLegService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the Flight Number
		int flight = StringUtils.parse(ctx.getParameter("flight"), 1);
		Airline a = SystemData.getAirline(ctx.getParameter("airline"));
		if (a == null)
			a = SystemData.getAirline(SystemData.get("airline.code"));
		
		int leg = 0;
		try {
			GetScheduleInfo dao = new GetScheduleInfo(ctx.getConnection());
			leg = dao.getNextLeg(a, flight);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Save the flight number
		re.setAttribute("airline", a.getCode());
		re.setAttribute("number", String.valueOf(flight));
		re.setAttribute("leg", String.valueOf(leg));

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