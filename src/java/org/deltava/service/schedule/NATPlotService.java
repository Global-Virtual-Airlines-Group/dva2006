// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.jdom.*;

import org.deltava.beans.navdata.NavigationDataBean;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;
import org.deltava.util.XMLUtils;

/**
 * A Web Service to return North Atlantic Track data
 * @author Luke
 * @verison 1.0
 * @since 1.0
 */

public class NATPlotService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the date
		Date dt = StringUtils.parseDate(ctx.getParameter("date"), ctx.getUser().getDateFormat());
		
		Collection<OceanicWaypoints> tracks = null;
		try {
			GetRoute dao = new GetRoute(ctx.getConnection());
			tracks = dao.getOceanicTrakcs(OceanicRoute.NAT, dt).values();
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);
		re.setAttribute("date", ctx.getParameter("date"));
		
		// Build the track data
		for (Iterator<OceanicWaypoints> i = tracks.iterator(); i.hasNext(); ) {
			OceanicWaypoints ow = i.next();
			boolean isEast = (ow.getDirection() == OceanicWaypoints.EAST);
			Element te = new Element("track");
			te.setAttribute("code", ow.getTrack());
			te.setAttribute("isEast", String.valueOf(isEast));
			te.setAttribute("color", isEast ? "#EEEEEE" : "#EEEE44");
			for (Iterator<NavigationDataBean> wi = ow.getWaypoints().iterator(); wi.hasNext(); ) {
				NavigationDataBean ndb = wi.next();
				Element we = XMLUtils.createElement("waypoint", ndb.getInfoBox(), true);
				we.setAttribute("code", ndb.getCode());
				we.setAttribute("lat", String.valueOf(ndb.getLatitude()));
				we.setAttribute("lng", String.valueOf(ndb.getLongitude()));
				we.setAttribute("color", ndb.getIconColor());
				te.addContent(we);
			}
			
			re.addContent(te);
		}

		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error");
		}
		
		return SC_OK;
	}

	/**
	 * Returns if the Web Service invocation is logged.
	 * @return FALSE
	 */
	public boolean isLogged() {
		return false;
	}
}