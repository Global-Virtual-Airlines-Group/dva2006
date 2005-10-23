// Copyright (c) 2005 Delta Virtual Airlines. All Rights Reserved.
package org.deltava.service;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import org.jdom.*;
import org.jdom.output.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.MapEntry;
import org.deltava.beans.navdata.*;

import org.deltava.dao.GetNavRoute;
import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;

/**
 * A Web Service to display flight routes with SID/STAR/Airway data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RouteMapService extends WebDataService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		LinkedList routePoints = null;
		try {
			GetNavRoute dao = new GetNavRoute(_con);
			routePoints = dao.getRouteWaypoints(ctx.getParameter("route"));
		} catch (DAOException de) {
			throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}

		// Convert to an XML document
		Document doc = formatPoints(routePoints);

		// Dump the XML to the output stream
		XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat().setEncoding("ISO-8859-1"));
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.println(xmlOut.outputString(doc));
			ctx.commit();
		} catch (IOException ie) {
			throw new ServiceException(HttpServletResponse.SC_CONFLICT, "I/O Error");
		}

		// Return success code
		return HttpServletResponse.SC_OK;
	}

	/**
	 * Helper method to convert route points into an XML document.
	 * @param points a List of MapEntry beans
	 * @return a JDOM XML document
	 */
	protected Document formatPoints(List points) {

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Calculate the distance and midpoint by taking the first/last waypoints
		GeoLocation mp = null;
		int distance = 500;
		if (points.size() > 1) {
			NavigationDataBean ndf = (NavigationDataBean) points.get(0);
			mp = ndf.getPosition().midPoint((GeoLocation) points.get(points.size() - 1));
			distance = ndf.getPosition().distanceTo((GeoLocation) points.get(points.size() - 1));
		} else if (points.size() == 1) {
			mp = (GeoLocation) points.get(0);
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
		for (Iterator i = points.iterator(); i.hasNext();) {
			MapEntry entry = (MapEntry) i.next();
			Element e = new Element("pos");
			e.setAttribute("lat", StringUtils.format(entry.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(entry.getLongitude(), "##0.00000"));
			e.setAttribute("color", entry.getIconColor());
			e.addContent(new CDATA(entry.getInfoBox()));
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