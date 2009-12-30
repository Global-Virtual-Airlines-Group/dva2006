// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.text.*;
import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.NavigationDataBean;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to search for navigation aids in a particular area.
 * @author Luke
 * @version 2.8
 * @since 2.1
 */

public class NavaidSearchService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the navaid to search for
		double lat = StringUtils.parse(ctx.getParameter("lat"), 0.0);
		double lng = StringUtils.parse(ctx.getParameter("lng"), 0.0);
		int range = Math.max(1000, StringUtils.parse(ctx.getParameter("range"), 150));
		boolean includeAirports = Boolean.valueOf(ctx.getParameter("airports")).booleanValue();
		
		// Build the location
		GeoLocation loc = new GeoPosition(lat, lng);
		Map<String, NavigationDataBean> results = new HashMap<String, NavigationDataBean>();
		try {
			GetNavData dao = new GetNavData(ctx.getConnection());
			dao.setQueryMax(1000);
          	results.putAll(dao.getObjects(loc, range));
          	results.putAll(dao.getIntersections(loc, range / 2));
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
		final NumberFormat df = new DecimalFormat("#0.000000");
		for (Iterator<NavigationDataBean> i = results.values().iterator(); i.hasNext(); ) {
			NavigationDataBean nd = i.next();
			if (includeAirports || (nd.getType() != NavigationDataBean.AIRPORT)) {
				Element we = new Element("waypoint");
				we.setAttribute("lat", df.format(nd.getLatitude()));
				we.setAttribute("lng", df.format(nd.getLongitude()));
				we.setAttribute("code", nd.getCode());
				we.setAttribute("color", nd.getIconColor());
				we.setAttribute("pal", String.valueOf(nd.getPaletteCode()));
				we.setAttribute("icon", String.valueOf(nd.getIconCode()));
				we.setAttribute("type", nd.getTypeName());
				we.addContent(new CDATA(nd.getInfoBox()));
				re.addContent(we);
			}
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
	
	/**
	 * Returns wether this web service requires authentication.
	 * @return TRUE always
	 */
	public boolean isSecure() {
		return true;
	}
}