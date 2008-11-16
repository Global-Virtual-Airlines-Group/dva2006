// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.util.zip.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.log4j.Logger;

import org.jdom.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;
import org.deltava.util.XMLUtils;

/**
 * A Web Service to format ACARS flight data for Google Earth.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class FlightDataEarthService extends GoogleEarthService {
	
	private static final Logger log = Logger.getLogger(FlightDataEarthService.class);
	
	/**
	 * Executes the Web Service, writing ACARS flight data in KML format.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check if we add position records
		boolean showData = Boolean.valueOf(ctx.getParameter("showData")).booleanValue();
		boolean showRoute = Boolean.valueOf(ctx.getParameter("showRoute")).booleanValue();

		// Set the maximum number of routes
		int maxFlights = showRoute ? 8 : 20;

		// Get the ACARS Flight IDs
		Collection<Integer> IDs = new TreeSet<Integer>();
		Collection<String> rawIDs = StringUtils.split(ctx.getParameter("id"), ",");
		for (Iterator<String> i = rawIDs.iterator(); (IDs.size() <= maxFlights) && i.hasNext(); ) {
			String rawID = i.next();
			try {
				IDs.add(new Integer(StringUtils.parseHex(rawID)));
			} catch (NumberFormatException nfe) {
				log.warn("Invalid ACARS flight ID - " + rawID);
			}
		}
		
		// Get ACARS data for the flights
		Collection<FlightInfo> flights = new TreeSet<FlightInfo>();
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAOs
			GetACARSData dao = new GetACARSData(con);
			GetNavData navdao = new GetNavData(con);
			for (Iterator<Integer> i = IDs.iterator(); i.hasNext(); ) {
				int id = i.next().intValue();
				FlightInfo info = dao.getInfo(id);
				if (info != null) {
					Collection<RouteEntry> routeData = dao.getRouteEntries(id, info.getArchived());
					info.setRouteData(routeData);
					if (showRoute) {
						List<String> routeEntries = StringUtils.split(info.getRoute(), " ");
						NavigationDataMap navaids = navdao.getByID(routeEntries);
						GeoPosition lastWaypoint = new GeoPosition(info.getAirportD());
						int distance = lastWaypoint.distanceTo(info.getAirportA());
						Collection<NavigationDataBean> routeInfo = new LinkedHashSet<NavigationDataBean>();
						
						// Load the SID waypoints if we have one
						if (info.getSID() != null)
							routeInfo.addAll(info.getSID().getWaypoints());
						
						// Filter out navaids and put them in the correct order
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
						
						// Load the STAR waypoints if we have one
						if (info.getSTAR() != null)
							routeInfo.addAll(info.getSTAR().getWaypoints());

						info.setPlanData(routeInfo);
					}
					
					// Save the flight
					flights.add(info);
				} else
					log.warn("Cannot find ACARS flight " + id);
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Build the XML document
		Document doc = KMLUtils.createKMLRoot();
		Element de = new Element("Document");
		doc.getRootElement().addContent(de);
		
		// Add the ACARS data
		int colorOfs = -1;
		for (Iterator<FlightInfo> i = flights.iterator(); i.hasNext(); ) {
			FlightInfo info = i.next();
			
			// Increment the line color
			colorOfs++;
			if (colorOfs >= COLORS.length)
				colorOfs = 0;
			
			// Generate the start/end airport and the route
			Collection<Element> results = new ArrayList<Element>();
			results.add(createAirport(info.getAirportD(), "Departed from " + info.getAirportD().getName()));
			results.add(createProgress(info.getRouteData(), COLORS[colorOfs]));
			results.add(createAirport(info.getAirportA(), "Landed at " + info.getAirportA().getName()));
			results.add(createPositionData(info.getRouteData(), showData));
			if (info.hasPlanData())
				results.add(createFlightRoute("Flight Plan", info.getPlanData(), false));

			// Add to the folder
			Element fe = de;
			if (info.hasRouteData()) {
				fe = new Element("Folder");
				fe.addContent(XMLUtils.createElement("name", "Flight " + info.getID()));
				KMLUtils.setVisibility(fe, true);
				de.addContent(fe);
			}
			
			// Add the children
			for (Iterator<Element> ci = results.iterator(); ci.hasNext(); )
				fe.addContent(ci.next());
		}
		
		// Clean up the namespace
		KMLUtils.copyNamespace(doc);
		
		// Determine the filename prefix
		String prefix = "acarsFlights";
		if (IDs.size() == 1) {
			Integer id = IDs.iterator().next();
			prefix = "acarsFlight" + id.toString();
		}

		// Determine if we compress the KML or not
		boolean noCompress = Boolean.valueOf(ctx.getParameter("noCompress")).booleanValue();
		try {
			if (noCompress) {
				ctx.getResponse().setContentType("application/vnd.google-earth.kml+xml");
				ctx.getResponse().setHeader("Content-disposition", "attachment; filename=" + prefix + ".kml");
				ctx.print(XMLUtils.format(doc, "UTF-8"));
				ctx.commit();
			} else {
				ctx.getResponse().setHeader("Content-disposition", "attachment; filename=" + prefix + ".kmz");
				ctx.getResponse().setContentType("application/vnd.google-earth.kmz kmz");
			
				// Create the ZIP output stream
				ZipOutputStream zout = new ZipOutputStream(ctx.getResponse().getOutputStream());
				zout.putNextEntry(new ZipEntry("acarsFlights.kml"));
				zout.write(XMLUtils.format(doc, "UTF-8").getBytes("UTF-8"));
				zout.closeEntry();
				zout.close();
			}

			// Flush the buffer
			ctx.getResponse().flushBuffer();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		// Return success code
		return SC_OK;
	}
}