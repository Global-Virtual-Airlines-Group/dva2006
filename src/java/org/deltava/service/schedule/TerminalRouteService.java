// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.jdom.*;

import org.deltava.beans.navdata.TerminalRoute;
import org.deltava.beans.schedule.Airport;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.XMLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display the SIDs and STARs for a particular Airport pair.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class TerminalRouteService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Load Airports
		Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
		Airport aA = SystemData.getAirport(ctx.getParameter("airportA"));
		
		List<TerminalRoute> tRoutes = new ArrayList<TerminalRoute>();
		try {
			GetNavRoute dao = new GetNavRoute(ctx.getConnection());
			tRoutes.addAll(new TreeSet<TerminalRoute>(dao.getRoutes(aD.getICAO(), TerminalRoute.SID)));
			tRoutes.addAll(new TreeSet<TerminalRoute>(dao.getRoutes(aA.getICAO(), TerminalRoute.STAR)));			
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
		for (Iterator<TerminalRoute> i = tRoutes.iterator(); i.hasNext();) {
			TerminalRoute tr = i.next();
			Element e = new Element(tr.getTypeName().toLowerCase());
			e.setAttribute("name", tr.getName());
			e.setAttribute("transition", tr.getTransition());
			e.setAttribute("code", tr.getCode());
			re.addContent(e);
		}
		
		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.getResponse().setCharacterEncoding("UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error");
		}
		
		// Return success code
		return SC_OK;
	}
}