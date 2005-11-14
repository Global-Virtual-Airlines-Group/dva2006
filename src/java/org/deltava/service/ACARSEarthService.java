// Copyright (c) 2005 Delta Virtual Airlines. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.jdom.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.dao.*;

import org.deltava.util.*;

/**
 * A Web Service to format ACARS flight data for Google Earth.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ACARSEarthService extends WebDataService {

	/**
	 * Executes the Web Service, writing ACARS flight data in KML format.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the ACARS Flight ID
		int id = 0;
		try {
			id = Integer.parseInt(ctx.getParameter("id"));
		} catch (NumberFormatException nfe) {
			throw new ServiceException(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID");
		}

		// Get the ACARS data
		List routeData = null;
		FlightInfo info = null;
		try {
			GetACARSData dao = new GetACARSData(_con);
			info = dao.getInfo(id);
			routeData = (info == null) ? Collections.EMPTY_LIST : dao.getRouteEntries(id, true, info.getArchived());
		} catch (DAOException de) {
			throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}
		
		// Build the XML document
		Document doc = new Document();
		Element ke = new Element("kml");
		doc.setRootElement(ke);
		Element de = new Element("Document");
		ke.addContent(de);
		
		// Generate the start airport placemark
		Airport aD = info.getAirportD();
		Element ade = new Element("Placemark");
		ade.addContent(XMLUtils.createElement("description", "Departed from " + aD.getName()));
		ade.addContent(XMLUtils.createElement("name", aD.getName() + " (" + aD.getICAO() + ")"));
		ade.addContent(XMLUtils.createElement("visibility", "1"));
		Element ads = new Element("Style");
		ads.addContent(addIcon(2, 0, 1)); // airplane icon
		ade.addContent(ads);
		Element adp = new Element("Point");
		adp.addContent(XMLUtils.createElement("coordinates", GeoUtils.format3D(aD)));
		ade.addContent(adp);
		ade.addContent(addLookAt(aD, 1500, -1));
		de.addContent(ade);
		
		// Format the route
		Element le = new Element("Placemark");
		le.addContent(XMLUtils.createElement("name", "Flight Route"));
		le.addContent(XMLUtils.createElement("description", String.valueOf(routeData.size()) + " Position Records"));
		le.addContent(XMLUtils.createElement("visibility", "1"));
		Element ls = new Element("Style");
		ls.addContent(XMLUtils.createElement("LineStyle", "color", "98a10000"));
		ls.addContent(XMLUtils.createElement("PolyStyle", "color", "2fa10000"));
		le.addContent(ls);
		Element lse = new Element("LineString");
		lse.addContent(XMLUtils.createElement("extrude", "1"));
		lse.addContent(XMLUtils.createElement("altitudeMode", "relativeToGround"));
		lse.addContent(XMLUtils.createElement("tessellate", "1"));
		
		// Format all of the coordinates
		StringBuilder buf = new StringBuilder();
		for (Iterator i = routeData.iterator(); i.hasNext(); ) {
			RouteEntry entry = (RouteEntry) i.next();
			buf.append(GeoUtils.format3D(entry, entry.getRadarAltitude()));
			buf.append(" \n");
		}
		
		// Save the coordinates
		lse.addContent(XMLUtils.createElement("coordinates", buf.toString()));
		le.addContent(lse);
		de.addContent(le);
		
		// Format the positions
		Element fe = new Element("Folder");
		fe.addContent(XMLUtils.createElement("name", "Position Records"));
		fe.addContent(XMLUtils.createElement("visibility", "0"));
		de.addContent(fe);
		int pos = 0;
		for (Iterator i = routeData.iterator(); i.hasNext(); ) {
			RouteEntry entry = (RouteEntry) i.next();
			Element pe = new Element("Placemark");
			pe.addContent(XMLUtils.createElement("name", "Route Point #" + String.valueOf(++pos)));
			pe.addContent(XMLUtils.createElement("description", entry.getInfoBox(), true));
			pe.addContent(XMLUtils.createElement("visibility", "0"));
			Element ps = new Element("Style");
			Element pis = addIcon(2, 0, 0); // info icon
			pis.addContent(XMLUtils.createElement("scale", "0.70"));
			pis.addContent(XMLUtils.createElement("heading", StringUtils.format(entry.getHeading(), "##0.00")));
			ps.addContent(pis);
			pe.addContent(ps);
			Element pp = new Element("Point");
			if (entry.isFlagSet(ACARSFlags.FLAG_ONGROUND)) {
				pp.addContent(XMLUtils.createElement("coordinates", GeoUtils.format2D(entry)));
				pp.addContent(XMLUtils.createElement("altitudeMode", "clampedToGround"));
			} else if (entry.getRadarAltitude() < 1000) {
				pp.addContent(XMLUtils.createElement("coordinates", GeoUtils.format3D(entry, entry.getRadarAltitude())));
				pp.addContent(XMLUtils.createElement("altitudeMode", "relativeToGround"));
			} else {
				pp.addContent(XMLUtils.createElement("coordinates", GeoUtils.format3D(entry, entry.getAltitude())));
				pp.addContent(XMLUtils.createElement("altitudeMode", "absolute"));
			}

			pe.addContent(pp);
			pe.addContent(addLookAt(entry, entry.getAltitude(), entry.getHeading()));
			fe.addContent(pe);
		}
		
		// Generate the end airport placemark
		Airport aA = info.getAirportA();
		Element aae = new Element("Placemark");
		aae.addContent(XMLUtils.createElement("description", "Landed at " + aA.getName()));
		aae.addContent(XMLUtils.createElement("name", aA.getName() + " (" + aA.getICAO() + ")"));
		aae.addContent(XMLUtils.createElement("visibility", "1"));
		Element aas = new Element("Style");
		aas.addContent(addIcon(2, 0, 1)); // airplane icon
		aae.addContent(aas);
		Element aap = new Element("Point");
		aap.addContent(XMLUtils.createElement("coordinates", GeoUtils.format3D(aA)));
		aae.addContent(aap);
		aae.addContent(addLookAt(aA, 1500, -1));
		de.addContent(aae);
		
		// Write the XML
		try {
			ctx.getResponse().setHeader("Content-disposition", "attachment; filename=acars" + id + ".kml");
			ctx.getResponse().setContentType("application/vnd.google-earth.kml+xml");
			ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
			ctx.commit();
		} catch (IOException ie) {
			throw new ServiceException(HttpServletResponse.SC_CONFLICT, "I/O Error");
		}
		
		// Return success code
		return HttpServletResponse.SC_OK;
	}

	/**
	 * Helper method to generate an icon element.
	 */
	private Element addIcon(int palette, int pX, int pY) {
		Element re = new Element("IconStyle");
		Element e = new Element("Icon");
		e.addContent(XMLUtils.createElement("href", "root://icons/palette-" + palette + ".png", true));
		e.addContent(XMLUtils.createElement("x", String.valueOf(pX *32)));
		e.addContent(XMLUtils.createElement("y", String.valueOf(pY *32)));
		e.addContent(XMLUtils.createElement("w", "32"));
		e.addContent(XMLUtils.createElement("h", "32"));
		re.addContent(e);
		return re;
	}
	
	/**
	 * Helper method to generate a LookAt element.
	 */
	private Element addLookAt(GeoLocation loc, int altitude, int heading) {
		Element e = new Element("LookAt");
		e.addContent(XMLUtils.createElement("longitude", StringUtils.format(loc.getLongitude(), "##0.0000")));
		e.addContent(XMLUtils.createElement("latitude", StringUtils.format(loc.getLatitude(), "##0.0000")));
		e.addContent(XMLUtils.createElement("range", StringUtils.format(altitude, "##0.000")));
		if (heading == -1) {
			e.addContent(XMLUtils.createElement("heading", "0.00"));
			e.addContent(XMLUtils.createElement("tilt", "85.0000"));
		} else {
			e.addContent(XMLUtils.createElement("heading", StringUtils.format(heading, "##0.00")));
			e.addContent(XMLUtils.createElement("tilt", "55.0000"));
		}

		return e;
	}
}