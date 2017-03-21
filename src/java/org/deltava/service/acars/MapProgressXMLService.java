// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom2.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.FlightInfo;
import org.deltava.beans.flight.Recorder;

import org.deltava.dao.*;
import org.deltava.dao.redis.GetTrack;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to provide XML-formatted ACARS progress data for Google Maps.
 * @author Luke
 * @version 7.1
 * @since 1.0
 */

public class MapProgressXMLService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the Flight ID
		int id = StringUtils.parse(ctx.getParameter("id"), 0);
		if (id < 1)
			return SC_NOT_FOUND;

		// Determine if we show the route
		boolean doRoute = Boolean.valueOf(ctx.getParameter("route")).booleanValue();

		// Get the DAO and the route data
		final List<GeoLocation> routePoints = new ArrayList<GeoLocation>();
		final Collection<MarkerMapEntry> routeWaypoints = new ArrayList<MarkerMapEntry>();
		final List<GeoLocation> tempPoints = new ArrayList<GeoLocation>();
		try {
			Connection con = ctx.getConnection();
			GetACARSPositions dao = new GetACARSPositions(con);
			FlightInfo info = dao.getInfo(id);
			if ((info != null) && (info.getFDR() == Recorder.XACARS))
				routePoints.addAll(dao.getXACARSEntries(id));
			else if (info != null)
				routePoints.addAll(dao.getRouteEntries(id, false, false));
			
			// Get temporary waypoints
			GetTrack tkdao = new GetTrack();
			tempPoints.addAll(tkdao.getTrack(id));
			if (!routePoints.isEmpty())
				tempPoints.add(0, routePoints.get(routePoints.size() - 1));

			// Load the route and the route waypoints
			if ((info != null) && doRoute) {
				Collection<MarkerMapEntry> wps = new LinkedHashSet<MarkerMapEntry>(); 
				GetNavRoute navdao = new GetNavRoute(con);
				wps.add(info.getAirportD());
				if (info.getRunwayD() != null)
					wps.add(info.getRunwayD());
				if (info.getSID() != null)
					wps.addAll(info.getSID().getWaypoints());
				wps.addAll(navdao.getRouteWaypoints(info.getRoute(), info.getAirportD()));
				if (info.getSTAR() != null)
					wps.addAll(info.getSTAR().getWaypoints());
				if (info.getRunwayA() != null)
					wps.add(info.getRunwayA());
				wps.add(info.getAirportA());
				
				// Trim spurious entries
				routeWaypoints.addAll(GeoUtils.stripDetours(wps, 150));
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Write the saved positions
		for (GeoLocation entry : routePoints) {
			Element e = new Element("pos");
			e.setAttribute("lat", StringUtils.format(entry.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(entry.getLongitude(), "##0.00000"));
			re.addContent(e);
		}
		
		// Write the temporary positions
		for (GeoLocation entry : tempPoints) {
			Element e = new Element("tpos");
			e.setAttribute("lat", StringUtils.format(entry.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(entry.getLongitude(), "##0.00000"));
			re.addContent(e);
		}

		// Write the route
		for (MapEntry entry : routeWaypoints) {
			Element e = XMLUtils.createElement("route", entry.getInfoBox(), true);
			e.setAttribute("lat", StringUtils.format(entry.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(entry.getLongitude(), "##0.00000"));
			if (entry instanceof MarkerMapEntry)
				e.setAttribute("color", ((MarkerMapEntry) entry).getIconColor());
			else {
				IconMapEntry ime = (IconMapEntry) entry;
				e.setAttribute("pal", String.valueOf(ime.getPaletteCode()));
				e.setAttribute("icon", String.valueOf(ime.getIconCode()));
			}
			
			re.addContent(e);
		}

		// Dump the XML to the output stream
		try {
			ctx.setContentType("text/xml", "UTF-8");
			ctx.setExpiry(5);
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	@Override
	public final boolean isLogged() {
		return false;
	}
}