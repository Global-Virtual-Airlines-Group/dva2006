// Copyright 2005, 2006, 2007, 2008, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display ACARS Flight Report data.
 * @author Luke
 * @version 3.2
 * @since 1.0
 */

public class MapFlightDataService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
   public int execute(ServiceContext ctx) throws ServiceException {
      
		// Get the Flight ID
		int id = StringUtils.parse(ctx.getParameter("id"), 0);
		
		// Get the DAO and the route data
		Collection<GeoLocation> routePoints = null;
		try {
			GetACARSData dao = new GetACARSData(ctx.getConnection());
			FlightInfo info = dao.getInfo(id);
			if (info != null)
				routePoints = dao.getRouteEntries(id, false, info.getArchived());
			else
				routePoints = Collections.emptyList();
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);
		
		// Write the positions - Gracefully handle geopositions - don't append a color and let the JS handle this
		for (Iterator<GeoLocation> i = routePoints.iterator(); i.hasNext(); ) {
			GeoLocation entry = i.next();
			
			Element e = null;
			if (entry instanceof MarkerMapEntry) {
				MarkerMapEntry me = (MarkerMapEntry) entry;
				e = XMLUtils.createElement("pos", me.getInfoBox(), true);
				e.setAttribute("color", me.getIconColor());
			} else if (entry instanceof IconMapEntry) {
				IconMapEntry me = (IconMapEntry) entry;
				e = XMLUtils.createElement("pos", me.getInfoBox(), true);
				e.setAttribute("pal", String.valueOf(me.getPaletteCode()));
				e.setAttribute("icon", String.valueOf(me.getIconCode()));
			} else
				e = new Element("pos");
			
			e.setAttribute("lat", StringUtils.format(entry.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(entry.getLongitude(), "##0.00000"));
			if (entry instanceof RouteEntry) {
				RouteEntry rte = (RouteEntry) entry;
				if (rte.getController() != null) {
					Element ae = new Element("atc");
					Controller ctr = rte.getController();
					ae.setAttribute("id", ctr.getCallsign());
					ae.setAttribute("type", String.valueOf(ctr.getFacility()));
					if ((ctr.getFacility() != Facility.CTR) && (ctr.getFacility() != Facility.FSS)) {
						ae.setAttribute("lat", StringUtils.format(ctr.getLatitude(), "##0.00000"));
						ae.setAttribute("lng", StringUtils.format(ctr.getLongitude(), "##0.00000"));
						ae.setAttribute("range", String.valueOf(ctr.getFacility().getRange()));
					}
					
					e.addContent(ae);
				}
			}
			
			re.addContent(e);
		}
		
		// Dump the XML to the output stream
		try {
			ctx.setContentType("text/xml", "UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
			return SC_OK;
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
   }

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	public final boolean isLogged() {
		return false;
	}
}