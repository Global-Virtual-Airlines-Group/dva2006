// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.Pilot;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to download
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class OnlineIDService extends WebService {
	
	/**
	 * Executes the Web Service, returning Pilot names and IDs.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Determine if we format using XML
		boolean isXML = Boolean.valueOf(ctx.getParameter("xml")).booleanValue();
		
		// Get the network name
		String network = ctx.getParameter("network");
		if (network != null) {
			network = network.toUpperCase();
			Collection networks = (Collection) SystemData.getObject("online.networks");
			if (!networks.contains(network))
				network = SystemData.get("online.default_network");
		} else
			network = SystemData.get("online.default_network");
		
		Collection<Pilot> pilots = null;
		try {
			Connection con = ctx.getConnection();
			
			// Load the pilots
			GetPilotOnline pdao = new GetPilotOnline(con);
			pilots = pdao.getPilots(network);
		} catch (DAOException de) {
			ServiceException se = error(500, de.getMessage(), de);
			se.setLogStackDump(false);
			throw se;
		} finally {
			ctx.release();
		}
		
		// If we're using XML, init the document
		Element re = null;
		Document doc = isXML ? new Document() : null;
		if (isXML) {
			re = new Element("pilots");
			doc.setRootElement(re);
		} else
			ctx.getResponse().setContentType("text/plain");
		
		// Write out the pilot list
		ctx.getResponse().setCharacterEncoding("UTF-8");
		for (Iterator<Pilot> i = pilots.iterator(); i.hasNext(); ) {
			Pilot p = i.next();
			if (isXML) {
				Element pe = new Element("pilot");
				pe.setAttribute("id", p.getNetworkIDs().get(network));
				pe.setAttribute("name", p.getName());
				re.addContent(pe);
			} else {
				ctx.print(p.getNetworkIDs().get(network));
				ctx.print(" ");
				ctx.println(p.getName());
			}
		}
		
		// If we're writing using XML, dump out the document
		if (isXML) {
			ctx.getResponse().setContentType("text/xml");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
		}
		
		try {
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error");
		}
		
		// Write result code
		return SC_OK;
	}
}