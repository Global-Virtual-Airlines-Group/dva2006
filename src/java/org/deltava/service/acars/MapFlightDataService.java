// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.FlightInfo;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display ACARS Flight Report data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MapFlightDataService extends WebDataService {

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
		
		// Get the DAO and the route data
		Collection<GeoLocation> routePoints = null;
		try {
			GetACARSData dao = new GetACARSData(_con);
			FlightInfo info = dao.getInfo(id);
			if (info != null)
				routePoints = dao.getRouteEntries(id, false, info.getArchived());
			else
				routePoints = Collections.emptyList();
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}
		
		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);
		
		// Write the positions - Gracefully handle geopositions - don't append a color and let the JS handle this
		for (Iterator<GeoLocation> i = routePoints.iterator(); i.hasNext(); ) {
			GeoLocation entry = i.next();
			Element e = null;
			if (entry instanceof MapEntry) {
				MapEntry me = (MapEntry) entry;
				e = XMLUtils.createElement("pos", me.getInfoBox(), true);
				e.setAttribute("color", me.getIconColor());
			} else {
				e = new Element("pos");
			}
			
			e.setAttribute("lat", StringUtils.format(entry.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(entry.getLongitude(), "##0.00000"));
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
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	public final boolean isLogged() {
		return false;
	}
}