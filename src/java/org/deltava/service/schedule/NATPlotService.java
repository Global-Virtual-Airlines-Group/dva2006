// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 2.1
 * @since 1.0
 */

public class NATPlotService extends WebService {

	private static final Collection<ConcordeNAT> CONC_ROUTES = Arrays.asList(new ConcordeNAT("M",
			"5015N,5020N,5030N,4840N,4750N"), new ConcordeNAT("N", "45/50,47/40,49/30,49/20,49/15"), 
			new ConcordeNAT("O", "48/15,48/20,48/30,46/40,44/50,42/60"));

	private static class ConcordeNAT extends OceanicWaypoints {

		ConcordeNAT(String track, String route) {
			super(NAT, new Date());
			setTrack(track);
			for (Iterator<String> i = StringUtils.split(route, ",").iterator(); i.hasNext();) {
				String wp = i.next();
				addWaypoint(Intersection.parseNAT(wp));
			}
		}

		public String getTrack() {
			return "S" + super.getTrack();
		}

		public boolean isFixed() {
			return true;
		}
	}

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
			tracks = new ArrayList<OceanicWaypoints>(dao.getOceanicTrakcs(OceanicRoute.NAT, dt).values());
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
		final NumberFormat nf = new DecimalFormat("##0.0000");
		tracks.addAll(CONC_ROUTES);
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

	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE
	 */
	public boolean isSecure() {
		return true;
	}
}