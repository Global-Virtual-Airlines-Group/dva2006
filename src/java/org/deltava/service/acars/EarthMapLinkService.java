// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.service.*;
import org.deltava.util.XMLUtils;

/**
 * A Web Service to generate a link to the Google Earth live ACARS map.
 * @author Luke
 * @version 1.0
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
		Document doc = new Document();
		Element ke = new Element("kml");
		doc.setRootElement(ke);
		Element de = XMLUtils.createElement("Document", "visibility", "1");
		ke.addContent(de);

		// Format the URL
		StringBuilder buf = new StringBuilder(ctx.getRequest().getRequestURL());
		buf.setLength(buf.lastIndexOf("/") + 1);

		// Create the progress network link entry
		Element nle = new Element("NetworkLink");
		nle.addContent(XMLUtils.createElement("name", "ACARS Live Map"));
		Element nlu = new Element("Url");
		nlu.addContent(XMLUtils.createElement("href", buf.toString() + "acars_map_eprog.ws"));
		nlu.addContent(XMLUtils.createElement("refreshMode", "onInterval"));
		nlu.addContent(XMLUtils.createElement("refreshInterval", "30"));
		nlu.addContent(XMLUtils.createElement("viewRefreshMode", "never"));
		nlu.addContent(XMLUtils.createElement("refreshVisibility", "0"));
		nle.addContent(nlu);
		de.addContent(nle);

		// Create the flight plan network link entry
		Element nple = new Element("NetworkLink");
		nple.addContent(XMLUtils.createElement("name", "ACARS Flight Plans"));
		Element plu = new Element("Url");
		plu.addContent(XMLUtils.createElement("href", buf.toString() + "acars_map_eplan.ws"));
		plu.addContent(XMLUtils.createElement("refreshMode", "onInterval"));
		plu.addContent(XMLUtils.createElement("refreshInterval", "360"));
		plu.addContent(XMLUtils.createElement("viewRefreshMode", "never"));
		plu.addContent(XMLUtils.createElement("refreshVisibility", "0"));
		nple.addContent(plu);
		de.addContent(nple);

		// Create FIR boundary network link entry
		Element fle = new Element("NetworkLink");
		fle.addContent(XMLUtils.createElement("name", "FIR Boundaries"));
		Element flu = new Element("Url");
		flu.addContent(XMLUtils.createElement("href", buf.toString() + "servinfo/firs.kmz"));
		flu.addContent(XMLUtils.createElement("viewRefreshMode", "never"));
		flu.addContent(XMLUtils.createElement("refreshVisibility", "0"));
		fle.addContent(flu);
		de.addContent(fle);

		// Write the XML
		try {
			ctx.getResponse().setHeader("Content-disposition", "attachment; filename=acarsMapLink.kml");
			ctx.getResponse().setContentType("application/vnd.google-earth.kml+xml");
			ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error");
		}

		// Return success code
		return SC_OK;
	}
}