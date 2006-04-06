// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import org.jdom.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to provide XML-formatted ACARS position data for Google Maps.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ACARSMapService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the ACARS connection Pool
		ACARSAdminInfo acarsPool = (ACARSAdminInfo) SystemData.getObject(SystemData.ACARS_POOL);

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Add the items
		for (Iterator<RouteEntry> i = acarsPool.getMapEntries().iterator(); i.hasNext();) {
			RouteEntry entry = i.next();
			Element e = new Element("aircraft");
			e.setAttribute("lat", StringUtils.format(entry.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(entry.getLongitude(), "##0.00000"));
			e.setAttribute("flight_id", String.valueOf(entry.getID()));
			e.setAttribute("color", entry.getIconColor());
			if (entry instanceof TabbedMapEntry) {
				TabbedMapEntry tme = (TabbedMapEntry) entry;
				e.setAttribute("tabs", String.valueOf(tme.getTabNames().size()));
				for (int x = 0; x < tme.getTabNames().size(); x++) {
					Element te = new Element("tab");
					te.setAttribute("name", tme.getTabNames().get(x));
					te.addContent(new CDATA(tme.getTabContents().get(x)));
					e.addContent(te);
				}
			} else {
				e.setAttribute("tabs", "0");
				e.addContent(new CDATA(entry.getInfoBox()));
			}

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

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	public final boolean isLogged() {
		return false;
	}
}