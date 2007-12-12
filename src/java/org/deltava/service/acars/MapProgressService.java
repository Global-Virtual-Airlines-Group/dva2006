// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.FlightInfo;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to provide XML-formatted ACARS progress data for Google Maps.
 * @author Luke
 * @version 2.0
 * @since 1.0
 */

public class MapProgressService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the Flight ID
		int id = StringUtils.parse(ctx.getParameter("id"), 0);
		if (id < 1)
			return SC_NOT_FOUND;

		// Determine if we show the route
		boolean doRoute = Boolean.valueOf(ctx.getParameter("route")).booleanValue();

		// Get the DAO and the route data
		FlightInfo info = null;
		Collection<GeoLocation> routePoints = null;
		Collection<MapEntry> routeWaypoints = new LinkedHashSet<MapEntry>();
		try {
			Connection con = ctx.getConnection();
			GetACARSData dao = new GetACARSData(con);
			routePoints = dao.getRouteEntries(id, false, false);

			// Load the route and the route waypoints
			info = dao.getInfo(id);
			if ((info != null) && doRoute) {
				GetNavRoute navdao = new GetNavRoute(con);
				routeWaypoints.add(info.getAirportD());
				if (info.getSID() != null)
					routeWaypoints.addAll(info.getSID().getWaypoints());
				routeWaypoints.addAll(navdao.getRouteWaypoints(info.getRoute()));
				if (info.getSTAR() != null)
					routeWaypoints.addAll(info.getSTAR().getWaypoints());
				routeWaypoints.add(info.getAirportA());
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Write the positions
		for (Iterator<GeoLocation> i = routePoints.iterator(); i.hasNext();) {
			GeoLocation entry = i.next();
			Element e = new Element("pos");
			e.setAttribute("lat", StringUtils.format(entry.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(entry.getLongitude(), "##0.00000"));
			re.addContent(e);
		}

		// Write the route
		for (Iterator<? extends MapEntry> i = routeWaypoints.iterator(); i.hasNext();) {
			MapEntry entry = i.next();
			Element e = XMLUtils.createElement("route", entry.getInfoBox(), true);
			e.setAttribute("lat", StringUtils.format(entry.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(entry.getLongitude(), "##0.00000"));
			e.setAttribute("color", entry.getIconColor());
			re.addContent(e);
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
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	public final boolean isLogged() {
		return false;
	}
}