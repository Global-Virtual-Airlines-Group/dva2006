// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.jdom.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Service to format ACARS flight data for Google Earth.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ACARSEarthService extends GoogleEarthService {
	
	private static final Logger log = Logger.getLogger(ACARSEarthService.class);
	
	/**
	 * Executes the Web Service, writing ACARS flight data in KML format.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the ACARS Flight IDs
		Collection<Integer> IDs = new TreeSet<Integer>();
		Collection<String> rawIDs = StringUtils.split(ctx.getParameter("id"), ",");
		for (Iterator<String> i = rawIDs.iterator(); i.hasNext();) {
			String rawID = i.next();
			try {
				IDs.add(new Integer(StringUtils.parseHex(rawID)));
			} catch (NumberFormatException nfe) {
				log.warn("Invalid ACARS flight ID - " + rawID);
			}
		}

		// Check if we add position records
		boolean showData = Boolean.valueOf(ctx.getParameter("showData")).booleanValue();
		boolean showRoute = Boolean.valueOf(ctx.getParameter("showRoute")).booleanValue();
		
		// Get the DAOs
		GetACARSData dao = new GetACARSData(_con);
		GetNavData navdao = new GetNavData(_con);

		// Get ACARS data for the flights
		Collection<FlightInfo> flights = new TreeSet<FlightInfo>();
		try {
			for (Iterator<Integer> i = IDs.iterator(); i.hasNext(); ) {
				int id = i.next().intValue();
				FlightInfo info = dao.getInfo(id);
				if (info != null) {
					@SuppressWarnings("unchecked")
					Collection<RouteEntry> routeData = dao.getRouteEntries(id, true, info.getArchived());
					info.setRouteData(routeData);
					
					if (showRoute) {
						List<String> routeEntries = StringUtils.split(info.getRoute(), " ");
						NavigationDataMap navaids = navdao.getByID(routeEntries);
						GeoPosition lastWaypoint = new GeoPosition(info.getAirportD());
						int distance = lastWaypoint.distanceTo(info.getAirportA());
						
						// Filter out navaids and put them in the correct order
						Collection<NavigationDataBean> routeInfo = new LinkedHashSet<NavigationDataBean>();
						for (Iterator<String> ri = routeEntries.iterator(); ri.hasNext();) {
							String navCode = ri.next();
							NavigationDataBean wPoint = navaids.get(navCode, lastWaypoint);
							if (wPoint != null) {
								if (lastWaypoint.distanceTo(wPoint) < distance) {
									routeInfo.add(wPoint);
									lastWaypoint.setLatitude(wPoint.getLatitude());
									lastWaypoint.setLongitude(wPoint.getLongitude());
								}
							}
						}

						info.setPlanData(routeInfo);
					}
					
					// Save the flight
					flights.add(info);
				} else {
					log.warn("Cannot find ACARS flight " + id);
				}
			}
		} catch (DAOException de) {
			throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}

		// Build the XML document
		Document doc = new Document();
		Element ke = new Element("kml");
		doc.setRootElement(ke);
		Element de = new Element("Document");
		ke.addContent(de);
		
		// Add the ACARS data
		int colorOfs = -1;
		for (Iterator<FlightInfo> i = flights.iterator(); i.hasNext(); ) {
			FlightInfo info = i.next();
			
			// Increment the line color
			colorOfs++;
			if (colorOfs >= COLORS.length)
				colorOfs = 0;
			
			Collection<Element> fData = createFlight(info, showData, COLORS[colorOfs]);
			Element fe = de;
			if (info.hasRouteData()) {
				fe = new Element("Folder");
				fe.addContent(XMLUtils.createElement("name", "Flight " + info.getID()));
				fe.addContent(XMLUtils.createElement("visibility", "1"));
				de.addContent(fe);
			}
			
			// Add the children
			for (Iterator<Element> ci = fData.iterator(); ci.hasNext(); )
				fe.addContent(ci.next());
		}

		// Write the XML
		try {
			ctx.getResponse().setHeader("Content-disposition", "attachment; filename=acarsFlights.kml");
			ctx.getResponse().setContentType("application/vnd.google-earth.kml+xml");
			ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
			ctx.commit();
		} catch (IOException ie) {
			throw new ServiceException(HttpServletResponse.SC_CONFLICT, "I/O Error");
		}

		// Return success code
		return HttpServletResponse.SC_OK;
	}
}