// Copyright 2005, 2006, 2007 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.io.IOException;
import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display flight routes with SID/STAR/Airway data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RouteMapService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		LinkedList<NavigationDataBean> routePoints = null;
		try {
			GetNavRoute dao = new GetNavRoute(ctx.getConnection());
			routePoints = dao.getRouteWaypoints(ctx.getParameter("route"));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Convert to an XML document
		Document doc = formatPoints(routePoints);

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
	 * Helper method to convert route points into an XML document.
	 * @param points a List of MapEntry beans
	 * @return a JDOM XML document
	 */
	protected Document formatPoints(List<NavigationDataBean> points) {

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Calculate the distance and midpoint by taking the first/last waypoints
		GeoLocation mp = null;
		int distance = 500;
		if (points.size() > 1) {
			NavigationDataBean ndf = points.get(0);
			mp = ndf.getPosition().midPoint(points.get(points.size() - 1));
			distance = ndf.getPosition().distanceTo(points.get(points.size() - 1));
		} else if (points.size() == 1) {
			mp = points.get(0);
		}

		// Save the midpoint
		if (mp != null) {
			Element mpe = new Element("midpoint");
			mpe.setAttribute("lat", StringUtils.format(mp.getLatitude(), "##0.00000"));
			mpe.setAttribute("lng", StringUtils.format(mp.getLongitude(), "##0.00000"));
			mpe.setAttribute("distance", StringUtils.format(distance, "###0"));
			re.addContent(mpe);
		}

		// Write the entries
		for (Iterator<NavigationDataBean> i = points.iterator(); i.hasNext();) {
			NavigationDataBean entry = i.next();
			Element e = XMLUtils.createElement("pos", entry.getInfoBox(), true);
			e.setAttribute("code", entry.getCode());
			e.setAttribute("lat", StringUtils.format(entry.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(entry.getLongitude(), "##0.00000"));
			e.setAttribute("color", entry.getIconColor());
			re.addContent(e);
		}

		// Return the document
		return doc;
	}

	/**
	 * Returns if the Web Service invocation is logged.
	 * @return FALSE
	 */
	public boolean isLogged() {
		return false;
	}
}