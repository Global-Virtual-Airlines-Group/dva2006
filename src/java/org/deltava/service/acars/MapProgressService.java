// Copyright 2005, 2006, 2007, 2008, 2009, 2011 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 4.1
 * @since 1.0
 */

public class MapProgressService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the Flight ID
		int id = StringUtils.parse(ctx.getParameter("id"), 0);
		if (id < 1)
			return SC_NOT_FOUND;

		// Determine if we show the route
		boolean doRoute = Boolean.valueOf(ctx.getParameter("route")).booleanValue();

		// Get the DAO and the route data
		final Collection<GeoLocation> routePoints = new ArrayList<GeoLocation>();
		final Collection<MarkerMapEntry> routeWaypoints = new ArrayList<MarkerMapEntry>();
		try {
			Connection con = ctx.getConnection();
			GetACARSData dao = new GetACARSData(con);
			FlightInfo info = dao.getInfo(id);
			if ((info != null) && info.isXACARS())
				routePoints.addAll(dao.getXACARSEntries(id));
			else
				routePoints.addAll(dao.getRouteEntries(id, false, false));

			// Load the route and the route waypoints
			if ((info != null) && doRoute) {
				Collection<MarkerMapEntry> wps = new LinkedHashSet<MarkerMapEntry>(); 
				GetNavRoute navdao = new GetNavRoute(con);
				wps.add(info.getAirportD());
				if (info.getRunwayD() != null)
					wps.add(info.getRunwayD());
				if (info.getSID() != null)
					wps.addAll(info.getSID().getWaypoints());
				wps.addAll(navdao.getRouteWaypoints(info.getRoute(), info.getAirportD()));
				if (info.getSTAR() != null)
					wps.addAll(info.getSTAR().getWaypoints());
				if (info.getRunwayA() != null)
					wps.add(info.getRunwayA());
				wps.add(info.getAirportA());
				
				// Trim spurious entries
				routeWaypoints.addAll(GeoUtils.stripDetours(wps, 50));
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
			if (entry instanceof MarkerMapEntry)
				e.setAttribute("color", ((MarkerMapEntry) entry).getIconColor());
			else {
				IconMapEntry ime = (IconMapEntry) entry;
				e.setAttribute("pal", String.valueOf(ime.getPaletteCode()));
				e.setAttribute("icon", String.valueOf(ime.getIconCode()));
			}
			
			re.addContent(e);
		}

		// Dump the XML to the output stream
		try {
			ctx.setContentType("text/xml", "UTF-8");
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