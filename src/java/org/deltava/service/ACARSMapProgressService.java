// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import org.jdom.*;
import org.jdom.output.*;

import org.deltava.beans.GeoLocation;

import org.deltava.dao.GetACARSData;
import org.deltava.dao.DAOException;
import org.deltava.util.StringUtils;

/**
 * A Web Service to provide XML-formatted ACARS progress data for Google Maps.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ACARSMapProgressService extends WebDataService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the Flight ID
		int id = 0;
		try {
			id = Integer.parseInt(ctx.getParameter("id"));
		} catch (NumberFormatException nfe) {
			return HttpServletResponse.SC_NOT_FOUND;
		}
		
		// Get the DAO and the route data
		Collection routePoints = null;
		try {
			GetACARSData dao = new GetACARSData(_con);
			routePoints = dao.getRouteEntries(id, false);
		} catch (DAOException de) {
			throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}
		
		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Write the positions
		for (Iterator i = routePoints.iterator(); i.hasNext(); ) {
			GeoLocation entry = (GeoLocation) i.next();
			Element e = new Element("pos");
			e.setAttribute("lat", StringUtils.format(entry.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(entry.getLongitude(), "##0.00000"));
			re.addContent(e);
		}
		
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
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	public final boolean isLogged() {
		return false;
	}
}