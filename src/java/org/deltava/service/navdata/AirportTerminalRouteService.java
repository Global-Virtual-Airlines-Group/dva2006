// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.text.*;
import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.XMLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display all Terminal Routes for an Airport.
 * @author Luke
 * @version 2.2
 * @since 2.1
 */

public class AirportTerminalRouteService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the airport
		Airport a = SystemData.getAirport(ctx.getParameter("airport"));
		if (a == null)
			throw error(SC_NOT_FOUND, "Unknown Airport - " + ctx.getParameter("airport"));

		final NumberFormat df = new DecimalFormat("#0.000000");
		Collection<TerminalRoute> routes = new ArrayList<TerminalRoute>();
		try {
			GetNavRoute dao = new GetNavRoute(ctx.getConnection());
			routes.addAll(dao.getRoutes(a.getICAO(), TerminalRoute.SID));
			routes.addAll(dao.getRoutes(a.getICAO(), TerminalRoute.STAR));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("routes");
		re.setAttribute("icao", a.getICAO());
		doc.setRootElement(re);

		// Generate the airport
		Element ae = new Element("airport");
		ae.setAttribute("icao", a.getICAO());
		ae.setAttribute("iata", a.getIATA());
		ae.setAttribute("lat", df.format(a.getLatitude()));
		ae.setAttribute("lng", df.format(a.getLongitude()));
		ae.setAttribute("pal", String.valueOf(a.getPaletteCode()));
		ae.setAttribute("icon", String.valueOf(a.getIconCode()));
		ae.setAttribute("color", a.getIconColor());
		ae.addContent(new CDATA(a.getInfoBox()));
		re.addContent(ae);

		// Format routes
		for (Iterator<TerminalRoute> i = routes.iterator(); i.hasNext();) {
			TerminalRoute tr = i.next();
			Element te = new Element("route");
			te.setAttribute("type", tr.getTypeName());
			
			// Build the ID
			String id = tr.getName() + "." + tr.getTransition();
			if (!"ALL".equals(tr.getRunway()))
				id += "." + tr.getRunway();
			
			te.setAttribute("id", id);
			te.addContent(XMLUtils.createElement("name", tr.getName()));
			te.addContent(XMLUtils.createElement("transition", tr.getTransition()));
			te.addContent(XMLUtils.createElement("runway", tr.getRunway()));
			Element twe = new Element("waypoints");
			for (Iterator<NavigationDataBean> wi = tr.getWaypoints().iterator(); wi.hasNext();) {
				NavigationDataBean nd = wi.next();
				Element we = new Element("waypoint");
				we.setAttribute("lat", df.format(nd.getLatitude()));
				we.setAttribute("lng", df.format(nd.getLongitude()));
				we.setAttribute("code", nd.getCode());
				we.setAttribute("color", nd.getIconColor());
				we.setAttribute("pal", String.valueOf(nd.getPaletteCode()));
				we.setAttribute("icon", String.valueOf(nd.getIconCode()));
				we.setAttribute("id", nd.toString());
				we.addContent(new CDATA(nd.getInfoBox()));
				twe.addContent(we);
			}

			te.addContent(twe);
			re.addContent(te);
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

	/**
	 * Returns wether this web service requires authentication.
	 * @return TRUE always
	 */
	public final boolean isSecure() {
		return true;
	}

	/**
	 * Returns wether this web service calls are logged. High volume services like the Map/ServInfo route services will
	 * not be logged.
	 * @return TRUE if invocation logging should be performed by the servlet, otherwise FALSE
	 */
	public final boolean isLogged() {
		return false;
	}
}