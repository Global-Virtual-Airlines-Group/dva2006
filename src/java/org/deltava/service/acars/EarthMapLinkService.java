// Copyright 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to generate a link to the Google Earth live ACARS map.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class EarthMapLinkService extends WebService {

	/**
	 * Executes the Web Service, writing ACARS flight data in KML format.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Build the XML document
		Document doc = KMLUtils.createKMLRoot();
		Element de = XMLUtils.createElement("Document", "visibility", "1");
		doc.getRootElement().addContent(de);

		// Format the URL
		StringBuilder buf = new StringBuilder(ctx.getRequest().getRequestURL());
		buf.setLength(buf.lastIndexOf("/") + 1);

		// Create the progress/flight plan network link entries
		de.addContent(createLink("ACARS Live Map", buf.toString() + "acars_map_eprog.ws", 30, true));
		de.addContent(createLink("ACARS Flight Plans", buf.toString() + "acars_map_eplan.ws", 360, false));
		
		// Create the NWS radar/cloud cover/daynight/METAR network link entries
		de.addContent(createLink("BlueMarble Earth", "http://www.gearthblog.com/kmfiles/bmngv2.kmz", 0, true));
		//de.addContent(createLink("US Radar Image", buf.toString() + "servinfo/nws_radar.kml", 0, false));
		//de.addContent(createLink("Cloud Cover", buf.toString() + "servinfo/clouds.kml", 0, false));
		de.addContent(createLink("Day/Night", buf.toString() + "servinfo/dayNight.kml", 0, false));

		// Create FIR boundary/DAFIF network link entries
		de.addContent(createLink("FIR Boundaries", buf.toString() + "servinfo/firs.kmz", 86400, false));
		de.addContent(createLink("DAFIF Data", buf.toString() + "servinfo/dafif.kmz", 0, false));

		// Fix namespaces
		KMLUtils.copyNamespace(doc);
		
		// Write the XML
		try {
			ctx.getResponse().setHeader("Content-disposition", "attachment; filename=acarsMapLink.kml");
			ctx.getResponse().setContentType("application/vnd.google-earth.kml+xml");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		// Return success code
		return SC_OK;
	}
	
	/**
	 * Helper method to generate the Url element.
	 */
	private Element createLink(String name, String url, int refreshSeconds, boolean isVisible) {
		Element le = new Element("NetworkLink");
		le.addContent(XMLUtils.createElement("name", name));
		KMLUtils.setVisibility(le, isVisible);
		Element e = new Element("Link");
		e.addContent(XMLUtils.createElement("href", url));
		if (refreshSeconds > 0) {
			e.addContent(XMLUtils.createElement("refreshMode", "onInterval"));
			e.addContent(XMLUtils.createElement("refreshInterval", String.valueOf(refreshSeconds)));
			e.addContent(XMLUtils.createElement("viewRefreshMode", "never"));
			e.addContent(XMLUtils.createElement("refreshVisibility", "0"));
		}
		
		le.addContent(e);
		return le;
	}
}