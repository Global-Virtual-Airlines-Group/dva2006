// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2012, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.util.zip.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.log4j.Logger;

import org.jdom2.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to format ACARS flight data for Google Earth.
 * @author Luke
 * @version 7.3
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
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check if we add position records
		boolean showData = Boolean.valueOf(ctx.getParameter("showData")).booleanValue();
		boolean showRoute = Boolean.valueOf(ctx.getParameter("showRoute")).booleanValue();

		// Set the maximum number of routes
		int maxFlights = showRoute ? 8 : 24;

		// Get the ACARS Flight IDs
		SortedSet<Integer> IDs = new TreeSet<Integer>();
		Collection<String> rawIDs = StringUtils.split(ctx.getParameter("id"), ",");
		if (rawIDs == null)
			return SC_NOT_FOUND;
		
		for (Iterator<String> i = rawIDs.iterator(); (IDs.size() <= maxFlights) && i.hasNext(); ) {
			String rawID = i.next();
			try {
				IDs.add(Integer.valueOf(StringUtils.parseHex(rawID)));
			} catch (NumberFormatException nfe) {
				log.warn("Invalid ACARS flight ID - " + rawID);
			}
		}
		
		// Get ACARS data for the flights
		Map<FlightInfo, Collection<Airspace>> flights = new TreeMap<FlightInfo, Collection<Airspace>>();
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAOs
			GetACARSPositions dao = new GetACARSPositions(con);
			GetNavData navdao = new GetNavData(con);
			GetAirspace asdao = new GetAirspace(con);
			for (Iterator<Integer> i = IDs.iterator(); i.hasNext(); ) {
				int id = i.next().intValue();
				FlightInfo info = dao.getInfo(id); Collection<Airspace> airspaces = new LinkedHashSet<Airspace>();
				if (info != null) {
					// FIXME: Whatabout XACARS?
					Collection<? extends RouteEntry> routeData = dao.getRouteEntries(id, info.getArchived()); 
					GeoLocation lastLoc = info.getAirportD();
					for (RouteEntry rt : routeData) {
						int distance = (rt.getRadarAltitude() < 2500) ? 2 : (rt.getGroundSpeed() / 120);
						if ((lastLoc == null) || (GeoUtils.distance(rt, lastLoc) > distance)) {
							airspaces.addAll(asdao.find(rt));
							airspaces.addAll(Airspace.findRestricted(rt, (rt.getGroundSpeed() / 90)));
							lastLoc = rt;
						}
					}
					
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
						for (String navCode : routeEntries) {
							NavigationDataBean wPoint = navaids.get(navCode, lastWaypoint);
							if (wPoint == null) continue;
							if (lastWaypoint.distanceTo(wPoint) < distance) {
								routeInfo.add(wPoint);
								lastWaypoint.setLatitude(wPoint.getLatitude());
								lastWaypoint.setLongitude(wPoint.getLongitude());
							}
						}
						
						// Load the STAR waypoints if we have one
						if (info.getSTAR() != null)
							routeInfo.addAll(info.getSTAR().getWaypoints());

						info.setPlanData(routeInfo);
					}
					
					flights.put(info, airspaces);
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
		for (Map.Entry<FlightInfo, Collection<Airspace>> me : flights.entrySet()) {
			FlightInfo info = me.getKey();
			
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
		String prefix = (IDs.size() == 1) ? ("acarsFlight" + IDs.first().toString()) : "acarsFlights";

		// Determine if we compress the KML or not
		boolean noCompress = Boolean.valueOf(ctx.getParameter("noCompress")).booleanValue();
		try {
			if (noCompress) {
				ctx.setContentType("application/vnd.google-earth.kml+xml");
				ctx.setHeader("Content-disposition", "attachment; filename=" + prefix + ".kml");
				ctx.print(XMLUtils.format(doc, "UTF-8"));
				ctx.commit();
			} else {
				ctx.setHeader("Content-disposition", "attachment; filename=" + prefix + ".kmz");
				ctx.setContentType("application/vnd.google-earth.kmz kmz");
			
				// Create the ZIP output stream
				try (ZipOutputStream zout = new ZipOutputStream(ctx.getResponse().getOutputStream())) {
					zout.putNextEntry(new ZipEntry("acarsFlights.kml"));
					zout.write(XMLUtils.format(doc, "UTF-8").getBytes("UTF-8"));
					zout.closeEntry();
				}
			}

			ctx.getResponse().flushBuffer();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}
}