// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.Pilot;

import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display pilots belonging to an online network for verification.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class OnlinePilotService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the maximum number of pilots to display
		int maxResults = StringUtils.parse(ctx.getParameter("maxResults"), 0);
		if (maxResults < 1)
			maxResults = 100;
		
		// Get the network name
		String network = ctx.getParameter("network");
		if (network != null) {
			network = network.toUpperCase();
			Collection networks = (Collection) SystemData.getObject("online.networks");
			if (!networks.contains(network))
				network = SystemData.get("online.default_network");
		} else
			network = SystemData.get("online.default_network");

		Collection<Pilot> results = null;
		try {
			GetPilotOnline dao = new GetPilotOnline(ctx.getConnection());
			dao.setQueryMax(maxResults);
			results = dao.getPilots(network);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("pilots");
		re.setAttribute("network", network);
		doc.setRootElement(re);
		
		// Generate the pilots
		for (Iterator<Pilot> i = results.iterator(); i.hasNext(); ) {
			Pilot p = i.next();
			Element pe = new Element("pilot");
			pe.setAttribute("id", p.getNetworkIDs().get(network));
			pe.addContent(XMLUtils.createElement("firstName", p.getFirstName()));
			pe.addContent(XMLUtils.createElement("lastName", p.getLastName()));
			re.addContent(pe);
		}
		
		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error");
		}
		
		// Return success code
		return SC_OK;
	}
}