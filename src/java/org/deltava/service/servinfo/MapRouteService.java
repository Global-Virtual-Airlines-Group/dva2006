// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.servinfo;

import java.io.*;
import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.file.GetServInfo;

import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to download ServInfo route data for Google Maps.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class MapRouteService extends WebService {

	/**
	 * Executes the Web Service, returning ServInfo route data.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the network name
		String networkName = ctx.getParameter("network");
		if (networkName == null)
			networkName = SystemData.get("online.default_network");
		
		// Get the network
		OnlineNetwork net = OnlineNetwork.valueOf(networkName.toUpperCase());

		// Get the network info from the cache
		NetworkInfo info = null;
		try {
			File f = new File(SystemData.get("online." + net.toString().toLowerCase() + ".local.info"));
			GetServInfo sidao = new GetServInfo(new FileInputStream(f));
			info = sidao.getInfo(net);
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}

		// Get the Pilot
		Pilot p = info.getPilot(ctx.getParameter("id"));
		if (p == null) 
			throw error(SC_NOT_FOUND, "Cannot find " + ctx.getParameter("id"), false);

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Generate the great circle route
		for (Iterator<GeoLocation> i = p.getRoute().iterator(); i.hasNext();) {
			GeoLocation loc = i.next();
			Element e = new Element("navaid");
			e.setAttribute("lat", StringUtils.format(loc.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(loc.getLongitude(), "##0.00000"));
			re.addContent(e);
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
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	public final boolean isLogged() {
		return false;
	}
}