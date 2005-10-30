// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import org.jdom.*;

import org.deltava.beans.MapEntry;
import org.deltava.beans.DatabaseBean;
import org.deltava.beans.acars.ACARSAdminInfo;

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
		for (Iterator i = acarsPool.getMapEntries().iterator(); i.hasNext();) {
			MapEntry entry = (MapEntry) i.next();
			Element e = XMLUtils.createElement("aircraft", entry.getInfoBox(), true);
			e.setAttribute("lat", StringUtils.format(entry.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(entry.getLongitude(), "##0.00000"));
			e.setAttribute("color", entry.getIconColor());
			if (entry instanceof DatabaseBean)
				e.setAttribute("flight_id", String.valueOf(((DatabaseBean) entry).getID()));
			
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