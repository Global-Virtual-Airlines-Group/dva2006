// Copyright 2007, 2008, 2009, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom2.*;

import org.deltava.beans.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to download Online Network IDs.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class OnlineIDService extends WebService {
	
	/**
	 * Executes the Web Service, returning Pilot names and IDs.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Determine if we format using XML
		boolean isXML = Boolean.valueOf(ctx.getParameter("xml")).booleanValue();
		
		// Get the network name
		String network = ctx.getParameter("network");
		if (network != null) {
			network = network.toUpperCase();
			Collection<?> networks = (Collection<?>) SystemData.getObject("online.networks");
			if (!networks.contains(network))
				network = SystemData.get("online.default_network");
		} else
			network = SystemData.get("online.default_network");
		
		// Get the network
		OnlineNetwork net = OnlineNetwork.valueOf(network.toUpperCase());
		
		Collection<Pilot> pilots = null;
		try {
			GetPilotOnline pdao = new GetPilotOnline(ctx.getConnection());
			pilots = pdao.getPilots(net);
		} catch (DAOException de) {
			ServiceException se = error(500, de.getMessage(), de);
			se.setLogStackDump(false);
			throw se;
		} finally {
			ctx.release();
		}
		
		// If we're using XML, init the document
		Element re = null;
		Document doc = null;
		if (isXML) {
			doc = new Document();
			re = new Element("pilots");
			doc.setRootElement(re);
		} else
			ctx.setContentType("text/plain", "UTF-8");
		
		// Write out the pilot list
		for (Iterator<Pilot> i = pilots.iterator(); i.hasNext(); ) {
			Pilot p = i.next();
			if (isXML && (re != null)) {
				Element pe = new Element("pilot");
				pe.setAttribute("id", p.getNetworkID(net));
				pe.setAttribute("name", p.getName());
				re.addContent(pe);
			} else {
				ctx.print(p.getNetworkID(net));
				ctx.print(" ");
				ctx.println(p.getName());
			}
		}
		
		// If we're writing using XML, dump out the document
		if (isXML) {
			ctx.setContentType("text/xml", "UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
		}
		
		try {
			ctx.setExpiry(3600);
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
}