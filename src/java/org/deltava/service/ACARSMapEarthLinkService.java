// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.util.XMLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to generate a link to the Google Earth live ACARS map.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ACARSMapEarthLinkService extends WebService {

	/**
	 * Executes the Web Service, writing ACARS flight data in KML format.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check if we display the flight plan
		boolean showRoute = Boolean.valueOf(ctx.getParameter("showRoute")).booleanValue();

		// Build the XML document
		Document doc = new Document();
		Element ke = new Element("kml");
		doc.setRootElement(ke);
		Element de = XMLUtils.createElement("Document", "visibility", "1");
		ke.addContent(de);
		
		// Format the URL
		StringBuilder buf = new StringBuilder(ctx.getRequest().getRequestURL());
		buf.setLength(buf.lastIndexOf("/") + 1);
		buf.append("acars_map_earth.ws?showRoute=");
		buf.append(showRoute);

		// Create the network link entry
		Element nle = new Element("NetworkLink");
		nle.addContent(XMLUtils.createElement("name", SystemData.get("airline.name") + " ACARS Live Map"));
		Element nlu = new Element("Url");
		nlu.addContent(XMLUtils.createElement("href", buf.toString()));
		nlu.addContent(XMLUtils.createElement("refreshMode", "onInterval"));
		nlu.addContent(XMLUtils.createElement("refreshInterval", "30"));
		nlu.addContent(XMLUtils.createElement("viewRefreshMode", "never"));
		nlu.addContent(XMLUtils.createElement("refreshVisibility", "0"));
		nle.addContent(nlu);
		de.addContent(nle);

		// Write the XML
		try {
			ctx.getResponse().setHeader("Content-disposition", "attachment; filename=acarsMapLink.kml");
			ctx.getResponse().setContentType("application/vnd.google-earth.kml+xml");
			ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
			ctx.commit();
		} catch (IOException ie) {
			throw new ServiceException(SC_CONFLICT, "I/O Error");
		}

		// Return success code
		return SC_OK;
	}
}