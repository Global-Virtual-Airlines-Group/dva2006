// Copyright 2007, 2008, 2009, 2010, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.text.*;
import java.time.Instant;
import java.util.*;
import java.io.IOException;

import org.jdom2.*;

import org.deltava.beans.MapEntry;
import org.deltava.beans.navdata.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to return Oceanic Track data.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class OceanicPlotService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the date
		Instant dt = null;
		try {
			dt = StringUtils.parseInstant(ctx.getParameter("date"), ctx.getUser().getDateFormat());
		} catch (Exception e) {
			// empty
		}
		
		// Get the track type
		OceanicTrackInfo.Type trackType = OceanicTrackInfo.Type.NAT;
		try {
			trackType = OceanicTrackInfo.Type.valueOf(ctx.getParameter("type").toUpperCase());
		} catch (Exception e) {
			// empty
		}

		DailyOceanicTracks tracks = null;
		try {
			GetOceanicRoute dao = new GetOceanicRoute(ctx.getConnection());
			tracks = dao.getOceanicTracks(trackType, dt);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Add concorde routes if NAT
		if (trackType == OceanicTrackInfo.Type.NAT) {
			for (OceanicTrack ot : OceanicTrack.CONC_ROUTES)
				tracks.addTrack(ot);
		}

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);
		re.setAttribute("date", StringUtils.format(tracks.getDate(), "MM/dd/yyyy"));

		// Build the track data
		final NumberFormat nf = new DecimalFormat("##0.0000");
		for (Iterator<OceanicTrack> i = tracks.getTracks().iterator(); i.hasNext();) {
			OceanicTrack ow = i.next();
			boolean isEast = (ow.getDirection() == OceanicTrackInfo.Direction.EAST);
			Element te = new Element("track");
			te.setAttribute("code", ow.getTrack());
			te.setAttribute("type", ow.isFixed() ? "C" : (isEast ? "E" : "W"));
			te.setAttribute("color", ow.isFixed() ? "#2040E0" : (isEast ? "#EEEEEE" : "#EEEE44"));
			te.setAttribute("track", ow.getRoute());
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
			ctx.setContentType("text/xml", "UTF-8");
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
	@Override
	public boolean isLogged() {
		return false;
	}

	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE
	 */
	@Override
	public boolean isSecure() {
		return true;
	}
}