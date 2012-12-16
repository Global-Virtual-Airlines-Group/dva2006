// Copyright 2008, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.jdom2.*;

import org.deltava.beans.navdata.TerminalRoute;
import org.deltava.beans.schedule.Airport;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.XMLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display the SIDs and STARs for a particular Airport pair.
 * @author Luke
 * @version 5.1
 * @since 2.2
 */

public class TerminalRouteService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Load Airports
		Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
		Airport aA = SystemData.getAirport(ctx.getParameter("airportA"));
		
		List<TerminalRoute> tRoutes = new ArrayList<TerminalRoute>();
		try {
			GetNavRoute dao = new GetNavRoute(ctx.getConnection());
			if (aD != null)
				tRoutes.addAll(new TreeSet<TerminalRoute>(dao.getRoutes(aD, TerminalRoute.Type.SID)));
			if (aA != null)
				tRoutes.addAll(new TreeSet<TerminalRoute>(dao.getRoutes(aA, TerminalRoute.Type.STAR)));			
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Create an XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);
		
		// Add SID/STAR names to XML document
		for (TerminalRoute tr : tRoutes) {
			Element e = new Element(tr.getType().name().toLowerCase());
			e.setAttribute("name", tr.getName());
			e.setAttribute("transition", tr.getTransition());
			e.setAttribute("code", tr.getCode());
			re.addContent(e);
		}
		
		// Dump the XML to the output stream
		try {
			ctx.setContentType("text/xml", "UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error");
		}
		
		return SC_OK;
	}
}