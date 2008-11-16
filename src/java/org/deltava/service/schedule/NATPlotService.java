// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.text.*;
import java.util.*;
import java.io.IOException;

import org.jdom.*;

import org.deltava.beans.MapEntry;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to return North Atlantic Track data.
 * @author Luke
 * @version 2.3
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
		Date dt = null;
		try {
			dt = StringUtils.parseDate(ctx.getParameter("date"), ctx.getUser().getDateFormat());
		} catch (Exception e) {
			// empty
		}

		List<OceanicWaypoints> tracks = null;
		try {
			GetRoute dao = new GetRoute(ctx.getConnection());
			tracks = new ArrayList<OceanicWaypoints>(dao.getOceanicTrakcs(OceanicRoute.NAT, dt).values());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Get the date from the tracks
		if ((dt == null) && !tracks.isEmpty())
			dt = tracks.get(0).getDate();

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);
		if (dt != null)
			re.setAttribute("date", StringUtils.format(dt, "MM/dd/yyyy"));

		// Build the track data
		final NumberFormat nf = new DecimalFormat("##0.0000");
		tracks.addAll(OceanicWaypoints.CONC_ROUTES);
		for (Iterator<OceanicWaypoints> i = tracks.iterator(); i.hasNext();) {
			OceanicWaypoints ow = i.next();
			boolean isEast = (ow.getDirection() == OceanicWaypoints.EAST);
			Element te = new Element("track");
			te.setAttribute("code", ow.getTrack());
			te.setAttribute("type", ow.isFixed() ? "C" : (isEast ? "E" : "W"));
			te.setAttribute("color", ow.isFixed() ? "#2040E0" : (isEast ? "#EEEEEE" : "#EEEE44"));
			te.setAttribute("track", ow.getWaypointCodes());
			for (Iterator<NavigationDataBean> wi = ow.getWaypoints().iterator(); wi.hasNext();) {
				NavigationDataBean ndb = wi.next();
				Element we = XMLUtils.createElement("waypoint", ndb.getInfoBox(), true);
				we.setAttribute("code", ndb.getCode());
				we.setAttribute("lat", nf.format(ndb.getLatitude()));
				we.setAttribute("lng", nf.format(ndb.getLongitude()));
				we.setAttribute("color", ow.isFixed() ? MapEntry.BLUE : (isEast ? MapEntry.WHITE : MapEntry.ORANGE));
				te.addContent(we);
			}

			re.addContent(te);
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

		return SC_OK;
	}

	/**
	 * Returns if the Web Service invocation is logged.
	 * @return FALSE
	 */
	public boolean isLogged() {
		return false;
	}

	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE
	 */
	public boolean isSecure() {
		return true;
	}
}