// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.jdom.*;

import org.deltava.beans.acars.DispatchRoute;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.wsdl.*;
import org.deltava.service.*;

import org.deltava.util.StringUtils;
import org.deltava.util.XMLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display the available Dispatch Routes between two Airports.
 * @author Luke
 * @version 2.4
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
		boolean doRoute = Boolean.valueOf(ctx.getParameter("fullRoute")).booleanValue();
		
		// Check for default runway
		String rwy = ctx.getParameter("runway");
		if ((rwy == null) || !rwy.startsWith("RW"))
			rwy = "ALL";
		
		// Get the Data
		Collection<FlightRoute> routes = new ArrayList<FlightRoute>();
		try {
			if (doFW) {
				GetFARoutes fwdao = new GetFARoutes();
				fwdao.setUser(SystemData.get("schedule.flightaware.download.user"));
				fwdao.setPassword(SystemData.get("schedule.flightaware.download.pwd"));
				routes.addAll(fwdao.getRouteData(aD, aA));
			}
			
			// Load from the database
			GetACARSRoute rdao = new GetACARSRoute(ctx.getConnection());
			routes.addAll(rdao.getRoutes(aD, aA, false));
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
			boolean isExternal = (rt instanceof ExternalFlightRoute); 
			StringBuilder buf = new StringBuilder();
			Element rte = new Element("route");
			rte.setAttribute("altitude", rt.getCruiseAltitude());
			rte.setAttribute("external", String.valueOf(isExternal));
			rte.addContent(XMLUtils.createElement("waypoints", rt.getRoute()));
			rte.addContent(XMLUtils.createElement("comments", rt.getComments(), true));
			if (rt.getSID() != null) {
				String sid = rt.getSID();
				if (sid.endsWith(".ALL"))
					sid = sid.replace("ALL", rwy);
				
				rte.setAttribute("sid", sid);
			}
				
			if (rt.getSTAR() != null)
				rte.setAttribute("star", rt.getSTAR());
			if (rt instanceof DispatchRoute) {
				rte.setAttribute("id", String.valueOf(rt.getID()));
				buf.append('#');
				buf.append(String.valueOf(rt.getID()));
			} else if (isExternal) {
				rte.setAttribute("id", "EXT" + String.valueOf(rt.getID()));
				buf.append("EXT");
				buf.append(String.valueOf(rt.getID()));
			}
			
			// Build the label
			buf.append(" - ");
			List<String> wps = StringUtils.split(rt.getRoute(), " ");
			if ((wps.size() > 10) && !doRoute && !isExternal) {
				buf.append(StringUtils.listConcat(wps.subList(0, 3), " "));
				buf.append(" ... ");
				buf.append(StringUtils.listConcat(wps.subList(wps.size() - 2, wps.size()), " "));
			} else
				buf.append(rt.getRoute());

			// Add the source
			if (isExternal) {
				buf.append(" (");
				buf.append(((ExternalFlightRoute) rt).getSource());
				buf.append(')');
			}
			
			// Add the element
			rte.addContent(XMLUtils.createElement("name", buf.toString(), true));
			re.addContent(rte);
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
}