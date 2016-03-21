// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.simfdr;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.util.stream.Collectors;

import org.jdom2.*;

import org.deltava.beans.schedule.Airport;
import org.deltava.comparators.AirportComparator;

import org.deltava.service.*;
import org.deltava.util.XMLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to serve valid Airports to simFDR.
 * @author Luke
 * @version 7.0
 * @since 7.0
 */

public class ValidAirportService extends SimFDRService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		authenticate(ctx);
		
		// Get the airports
		List<Airport> airports = SystemData.getAirports().values().stream().filter(a -> !a.getAirlineCodes().isEmpty()).collect(Collectors.toList());
		airports.sort(new AirportComparator(AirportComparator.ICAO));
		
		// Create the XML Document
		Document doc = new Document();
		Element re = new Element("airports");
		doc.setRootElement(re);
		for (Airport a : airports) {
			Element ae = new Element("airport");
			ae.setAttribute("iata", a.getIATA());
			ae.setAttribute("icao", a.getICAO());
			re.addContent(ae);
		}
		
		// Dump the XML to the output stream
		try {
			ctx.setContentType("text/xml", "UTF-8");
			ctx.setExpiry(7200);
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE always
	 */
	@Override
	public final boolean isLogged() {
		return false;
	}
}