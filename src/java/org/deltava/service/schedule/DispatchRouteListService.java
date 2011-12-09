// Copyright 2008, 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.jdom.*;

import org.deltava.beans.acars.DispatchRoute;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display the available Dispatch Routes between two Airports.
 * @author Luke
 * @version 4.1
 * @since 2.2
 */

public class DispatchRouteListService extends WebService {
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the airports
		Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
		Airport aA = SystemData.getAirport(ctx.getParameter("airportA"));
		
		// Check if loading from FlightAware
		boolean doFA = Boolean.valueOf(ctx.getParameter("external")).booleanValue() && SystemData.getBoolean("schedule.flightaware.enabled");
		boolean hasFARole = ctx.isUserInRole("Route") || ctx.isUserInRole("Dispatch") || ctx.isUserInRole("Operations");
		boolean doRoute = Boolean.valueOf(ctx.getParameter("fullRoute")).booleanValue();
		boolean forceFAReload = hasFARole && Boolean.valueOf(ctx.getParameter("faReload")).booleanValue();
		
		// Check for default runway
		String rwy = ctx.getParameter("runway");
		if ((rwy != null) && !rwy.startsWith("RW"))
			rwy = null;
		
		// Get the Data
		Collection<FlightRoute> routes = new ArrayList<FlightRoute>();
		try {
			RouteLoadHelper helper = new RouteLoadHelper(ctx.getConnection(), new ScheduleRoute(aD, aA));
			helper.setPreferredRunway(rwy);
			helper.loadDispatchRoutes();
			
			// Load cached routes
			helper.loadCachedRoutes();
			
			// Load flight aware routes
			if (forceFAReload || (doFA && hasFARole && !helper.hasRoutes()))
				helper.loadFlightAwareRoutes(true);
			
			// Load PIREP routes
			if (!helper.hasRoutes())
				helper.loadPIREPRoutes();
			
			// Get the weather
			helper.loadWeather();
			
			// Fix the SID/STAR
			helper.calculateBestTerminalRoute();
			routes.addAll(helper.getRoutes());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), true);
		} finally {
			ctx.release();
		}
		
		// Create the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);
		re.setAttribute("airportD", aD.getICAO());
		re.setAttribute("airportA", aA.getICAO());
		
		// Save the routes, stripping out duplicates
		Collection<String> rts = new HashSet<String>();
		for (Iterator<? extends FlightRoute> i = routes.iterator(); i.hasNext(); ) {
			FlightRoute rt = i.next();
			if (!rts.add(rt.getRoute()))
				continue;
			
			boolean isExternal = (rt instanceof ExternalFlightRoute); 
			StringBuilder buf = new StringBuilder();
			Element rte = new Element("route");
			rte.setAttribute("altitude", rt.getCruiseAltitude());
			rte.setAttribute("external", String.valueOf(isExternal));
			rte.addContent(XMLUtils.createElement("waypoints", rt.getRoute()));
			rte.addContent(XMLUtils.createElement("comments", rt.getComments(), true));
			if (rt.getSID() != null) {
				String sid = rt.getSID();
				if (sid.endsWith(".ALL") && (rwy != null))
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
			List<String> wps = StringUtils.split(rt.toString(), " ");
			if ((wps.size() > 10) && !doRoute && !isExternal) {
				buf.append(StringUtils.listConcat(wps.subList(0, 3), " "));
				buf.append(" ... ");
				buf.append(StringUtils.listConcat(wps.subList(wps.size() - 2, wps.size()), " "));
			} else
				buf.append(rt.toString());

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
			ctx.setContentType("text/xml", "UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error", false);
		}
		
		return SC_OK;
	}
}