// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display plotted flight routes with SID/STAR/Airway data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RoutePlotMapService extends RouteMapService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		List<TerminalRoute> tRoutes = new ArrayList<TerminalRoute>();
		Collection<NavigationDataBean> routePoints = new LinkedHashSet<NavigationDataBean>();
		try {
			GetNavRoute dao = new GetNavRoute(ctx.getConnection());
			
			// Translate IATA to ICAO codes
			String airportDCode = txIATA(ctx.getParameter("airportD"));
			String airportACode = txIATA(ctx.getParameter("airportA"));

			// Get the departure/arrival airports
			AirportLocation aD = dao.getAirport(airportDCode);
			AirportLocation aA = dao.getAirport(airportACode);

			// Add the departure airport
			if (aD != null) {
				routePoints.add(aD);
				Set<TerminalRoute> sids = new TreeSet<TerminalRoute>(dao.getRoutes(aD.getCode(), TerminalRoute.SID));
				tRoutes.addAll(sids);
			}

			// Check if we have a SID
			if (!StringUtils.isEmpty(ctx.getParameter("sid"))) {
				TerminalRoute sid = dao.getRoute(ctx.getParameter("sid"));
				if ((sid != null) && (aD != null) && (sid.getICAO().equals(aD.getCode()))) {
					NavigationDataMap sidMap = dao.getByID(sid.getWaypoints());
					routePoints.addAll(sid.getWaypoints(sidMap, aD));
				}
			}

			// Add the route waypoints
			if (!StringUtils.isEmpty(ctx.getParameter("route"))) {
				GeoLocation lastLoc = CollectionUtils.getLast(routePoints);
				Collection<String> wPoints = StringUtils.split(ctx.getParameter("route"), " ");
				NavigationDataMap ndmap = dao.getByID(wPoints);
				for (Iterator<String> i = wPoints.iterator(); i.hasNext(); ) {
					String wp = i.next();
					NavigationDataBean point = ndmap.get(wp, lastLoc);
					if (point != null) {
						routePoints.add(point);
						lastLoc = point;
					}
				}
			}

			// Check if we have a STAR
			if (!StringUtils.isEmpty(ctx.getParameter("star"))) {
				TerminalRoute star = dao.getRoute(ctx.getParameter("star"));
				if ((star != null) && (aA != null) && (star.getICAO().equals(aA.getCode()))) {
					NavigationDataMap starMap = dao.getByID(star.getWaypoints());
					routePoints.addAll(star.getWaypoints(starMap, aA));
				}
			}

			// Add the arrival airport
			if (aA != null) {
				routePoints.add(aA);
				Set<TerminalRoute> stars = new TreeSet<TerminalRoute>(dao.getRoutes(aA.getCode(), TerminalRoute.STAR));
				tRoutes.addAll(stars);
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Convert the points into a List
		List<NavigationDataBean> points = new ArrayList<NavigationDataBean>(routePoints);

		// Convert points to an XML document
		Document doc = formatPoints(points);
		Element re = doc.getRootElement();

		// Add SID/STAR names to XML document
		for (Iterator<TerminalRoute> i = tRoutes.iterator(); i.hasNext();) {
			TerminalRoute tr = i.next();
			Element e = new Element(tr.getTypeName().toLowerCase());
			e.setAttribute("name", tr.getName());
			e.setAttribute("transition", tr.getTransition());
			e.setAttribute("code", tr.getCode());
			re.addContent(e);
		}

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
	
	/**
	 * Helper method to translate IATA to ICAO codes.
	 */
	private String txIATA(String code) {
		
		if ((code != null) && (code.length() == 3)) {
			Airport aD = SystemData.getAirport(code);
			if (aD != null)
				return aD.getICAO();
		}
		
		return code;
	}
}