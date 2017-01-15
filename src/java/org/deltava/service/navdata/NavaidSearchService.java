// Copyright 2008, 2009, 2012, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.text.*;
import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom2.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to search for navigation aids in a particular area.
 * @author Luke
 * @version 6.0
 * @since 2.1
 */

public class NavaidSearchService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the navaid to search for
		int range = Math.min(1000, StringUtils.parse(ctx.getParameter("range"), 150));
		
		// Build the location
		GeoLocation loc = new GeoPosition(StringUtils.parse(ctx.getParameter("lat"), 0.0), StringUtils.parse(ctx.getParameter("lng"), 0.0));
		Collection<NavigationDataBean> results = new LinkedHashSet<NavigationDataBean>();
		try {
			GetNavData dao = new GetNavData(ctx.getConnection());
			dao.setQueryMax(1250);
          	results.addAll(dao.getObjects(loc, range));
          	results.addAll(dao.getIntersections(loc, range));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);
		
		// Format navaids
		boolean includeAirports = Boolean.valueOf(ctx.getParameter("airports")).booleanValue();
		final NumberFormat df = new DecimalFormat("#0.000000");
		for (NavigationDataBean nd : results) {
			if (includeAirports || (nd.getType() != Navaid.AIRPORT)) {
				Element we = XMLUtils.createElement("waypoint", nd.getInfoBox(), true);
				we.setAttribute("lat", df.format(nd.getLatitude()));
				we.setAttribute("lng", df.format(nd.getLongitude()));
				we.setAttribute("code", nd.getCode());
				we.setAttribute("color", nd.getIconColor());
				we.setAttribute("pal", String.valueOf(nd.getPaletteCode()));
				we.setAttribute("icon", String.valueOf(nd.getIconCode()));
				we.setAttribute("type", nd.getType().getName());
				re.addContent(we);
			}
		}
		
		// Dump the XML to the output stream
		try {
			ctx.setContentType("text/xml", "UTF-8");
			ctx.setExpiry(3600);
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}
	
	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE always
	 */
	@Override
	public boolean isSecure() {
		return true;
	}
}