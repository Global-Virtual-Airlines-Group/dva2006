// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.jdom.*;

import org.deltava.beans.AuthoredBean;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.wsdl.*;
import org.deltava.service.*;

import org.deltava.util.XMLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display the available Dispatch Routes between two Airprots.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class DispatchRouteListService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the airports
		Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
		Airport aA = SystemData.getAirport(ctx.getParameter("airportA"));
		
		// Check if loading from FlightAware
		boolean doFW = Boolean.valueOf(ctx.getParameter("external")).booleanValue();
		doFW &= ctx.isUserInRole("Route") && SystemData.getBoolean("schedule.flightaware.enabled");
		
		// Get the Data
		Collection<FlightRoute> routes = new ArrayList<FlightRoute>();
		try {
			if (doFW) {
				GetFlightAware fwdao = new GetFlightAware();
				routes.addAll(fwdao.getRouteData(aD, aA));
			}
			
			// Load from the database
			GetACARSRoute rdao = new GetACARSRoute(ctx.getConnection());
			routes.addAll(rdao.getRoutes(aD, aA));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Create the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);
		re.setAttribute("airportD", aD.getICAO());
		re.setAttribute("airportA", aA.getICAO());
		
		// Save the routes
		for (Iterator<? extends FlightRoute> i = routes.iterator(); i.hasNext(); ) {
			FlightRoute rt = i.next();
			Element rte = new Element("route");
			rte.setAttribute("id", String.valueOf(rt.getID()));
			rte.setAttribute("altitude", rt.getCruiseAltitude());
			rte.addContent(XMLUtils.createElement("waypoints", rt.getRoute()));
			rte.addContent(XMLUtils.createElement("comments", rt.getComments(), true));
			rte.setAttribute("external", String.valueOf(!(rt instanceof AuthoredBean)));
			re.addContent(rte);
		}
		
		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.getResponse().setCharacterEncoding("UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error");
		}
		
		// Return success code
		return SC_OK;
	}
}