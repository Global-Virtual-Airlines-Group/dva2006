// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.log4j.Logger;

import org.jdom.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.system.*;

import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to render the ACARS Map in Google Earth.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ACARSMapEarthService extends GoogleEarthService {
	
	private static final Logger log = Logger.getLogger(ACARSMapEarthService.class);

	/**
	 * Executes the Web Service, writing ACARS flight data in KML format.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check if we display the flight plan
		boolean showRoute = Boolean.valueOf(ctx.getParameter("showRoute")).booleanValue();

		// Get the ACARS flights currently in progress
		ACARSAdminInfo acarsPool = (ACARSAdminInfo) SystemData.getObject(SystemData.ACARS_POOL);
		Collection<Integer> ids = acarsPool.getFlightIDs();

		// Load the flight information
		Map<Integer, Pilot> pilots = new HashMap<Integer, Pilot>();
		Collection<FlightInfo> flights = new TreeSet<FlightInfo>();
		try {
			GetACARSData dao = new GetACARSData(_con);
			GetNavData navdao = new GetNavData(_con);
			
			// Loop through the flights
			Collection<Integer> userIDs = new HashSet<Integer>();
			for (Iterator<Integer> i = ids.iterator(); i.hasNext(); ) {
				int flightID = i.next().intValue();
				FlightInfo info = dao.getInfo(flightID);
				if (info != null) {
					userIDs.add(new Integer(info.getPilotID()));
					Collection<RouteEntry> routeData = dao.getRouteEntries(flightID, info.getArchived());
					info.setRouteData(routeData);
					
					// Load the flight plan
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
					
					// Add the flight information
					flights.add(info);
				}
			}
			
			// Load the user data
			GetUserData uddao = new GetUserData(_con);
			UserDataMap udmap = uddao.get(userIDs);
			
			// Add into the pilot map
			GetPilot pdao = new GetPilot(_con);
			for (Iterator<String> i = udmap.getTableNames().iterator(); i.hasNext(); ) {
				String tableName = i.next();
				if (UserDataMap.isPilotTable(tableName))
					pilots.putAll(pdao.getByID(userIDs, tableName));
			}
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
			return SC_INTERNAL_SERVER_ERROR;
		}
		
		// Create the flights and routes folders
		Map<String, Element> folders = new LinkedHashMap<String, Element>();
		folders.put("positions", XMLUtils.createElement("Folder", "name", "Aircraft"));
		folders.put("progress", XMLUtils.createElement("Folder", "name", "Flight Progress"));
		folders.put("plans", XMLUtils.createElement("Folder", "name", "Flight Plans"));
		folders.put("airports", XMLUtils.createElement("Folder", "name", "Airports"));
		
		// Convert the flight data to KML
		int colorOfs = -1;
		Collection<Airport> airports = new TreeSet<Airport>();
		for (Iterator<FlightInfo> i = flights.iterator(); i.hasNext(); ) {
			FlightInfo info = i.next();
			Pilot usr = pilots.get(new Integer(info.getPilotID()));
			
			// Get the flight name and increment the line color 
			String name = "Flight " + info.getID();
			colorOfs++;
			if (colorOfs >= COLORS.length)
				colorOfs = 0;
			
			// Save the airports
			airports.add(info.getAirportD());
			airports.add(info.getAirportA());
			
			// Add the position
			String usrName = (usr == null) ? info.getFlightCode() : usr.getName() + " (" + usr.getPilotCode() + ")";
			folders.get("positions").addContent(createAircraft(usrName, info.getPosition()));
			
			// Add the flight progress
			if (info.hasRouteData()) {
				Element fpe = createProgress(info.getRouteData(), COLORS[colorOfs]);
				KMLUtils.setVisibility(fpe, false);
				XMLUtils.setChildText(fpe, "name", usrName);
				folders.get("progress").addContent(fpe);
			}

			// Create the flight route entry
			if (info.hasPlanData()) {
				Element fre = createFlightRoute(name, info.getPlanData(), true);
				KMLUtils.setVisibility(fre, false);
				XMLUtils.setChildText(fre, "name", usrName);
				folders.get("plans.").addContent(fre);
			}
		}
		
		// Render the airports
		Element afe = folders.get("airports");
		for (Iterator<Airport> i = airports.iterator(); i.hasNext(); ) {
			Airport a = i.next();
			Element ae = createAirport(a, a.toString());
			KMLUtils.setVisibility(ae, false);
			afe.addContent(ae);
		}
		
		// Build the XML document
		Document doc = new Document();
		Element ke = new Element("kml");
		doc.setRootElement(ke);
		Element de = new Element("Document");
		ke.addContent(de);
		
		// Add the folders to the document
		for (Iterator<Element> i = folders.values().iterator(); i.hasNext(); ) {
			Element fe = i.next();
			de.addContent(fe);
		}
		
		// Write the XML
		try {
			ctx.getResponse().setHeader("Content-disposition", "attachment; filename=acarsMap.kml");
			ctx.getResponse().setContentType("application/vnd.google-earth.kml+xml");
			ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
			ctx.commit();
		} catch (IOException ie) {
			throw new ServiceException(SC_CONFLICT, "I/O Error");
		}

		// Return success code
		return SC_OK;
	}

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	public final boolean isLogged() {
		return false;
	}
}