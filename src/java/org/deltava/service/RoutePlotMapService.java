// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import org.jdom.*;

import org.deltava.beans.navdata.*;

import org.deltava.dao.GetNavRoute;
import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.XMLUtils;

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

		List tRoutes = new ArrayList();
		Set routePoints = new LinkedHashSet();
		try {
			GetNavRoute dao = new GetNavRoute(_con);

			// Get the departure/arrival airports
			AirportLocation aD = dao.getAirport(ctx.getParameter("airportD"));
			AirportLocation aA = dao.getAirport(ctx.getParameter("airportA"));

			// Add the departure airport
			if (aD != null) {
				routePoints.add(aD);
				tRoutes.addAll(dao.getRoutes(aD.getCode(), TerminalRoute.SID));
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
			if (!StringUtils.isEmpty(ctx.getParameter("route")))
				routePoints.addAll(dao.getRouteWaypoints(ctx.getParameter("route")));

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
				tRoutes.addAll(dao.getRoutes(aA.getCode(), TerminalRoute.STAR));
			}
		} catch (DAOException de) {
			throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}

		// Convert points to an XML document
		Document doc = formatPoints(new ArrayList(routePoints));
		Element re = doc.getRootElement();

		// Add SID/STAR names to XML document
		for (Iterator i = tRoutes.iterator(); i.hasNext();) {
			TerminalRoute tr = (TerminalRoute) i.next();
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
			throw new ServiceException(HttpServletResponse.SC_CONFLICT, "I/O Error");
		}

		// Return success code
		return HttpServletResponse.SC_OK;
	}
}