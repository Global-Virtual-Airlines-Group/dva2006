// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.log4j.Logger;

import org.jdom.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.GeoPosition;

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

		Collection<FlightInfo> flights = new TreeSet<FlightInfo>();
		try {
			GetACARSData dao = new GetACARSData(_con);
			GetNavData navdao = new GetNavData(_con);
			
			// Loop through the flights
			for (Iterator<Integer> i = ids.iterator(); i.hasNext(); ) {
				int flightID = i.next().intValue();
				FlightInfo info = dao.getInfo(flightID);
				if (info != null) {
					@SuppressWarnings("unchecked")
					Collection<RouteEntry> routeData = dao.getRouteEntries(flightID, true, info.getArchived());
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
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
			return SC_INTERNAL_SERVER_ERROR;
		}
		
		// Build the XML document
		Document doc = new Document();
		Element ke = new Element("kml");
		doc.setRootElement(ke);
		Element de = new Element("Document");
		ke.addContent(de);
		
		// Create the flights and routes folders
		Element fe = XMLUtils.createElement("Folder", "name", "Flights"); 
		Element fre = XMLUtils.createElement("Folder", "name", "Flight Routes");
		de.addContent(fe);
		de.addContent(fre);
		
		// Convert the flight data to KML
		int colorOfs = -1;
		for (Iterator<FlightInfo> i = flights.iterator(); i.hasNext(); ) {
			FlightInfo info = i.next();
			String name = "Flight " + info.getID();
			
			// Increment the line color
			colorOfs++;
			if (colorOfs >= COLORS.length)
				colorOfs = 0;

			// Create the flight route entry
			if (info.hasPlanData()) {
				fre.addContent(createFlightRoute(name, info.getPlanData(), true));
				info.getPlanData().clear();
			}
			
			// Create the flight data entry
			if (info.hasRouteData()) {
				Collection<Element> fData = createFlight(info, false, COLORS[colorOfs]);
				Element ffe = new Element("Folder");
				ffe.addContent(XMLUtils.createElement("name", name));
				ffe.addContent(XMLUtils.createElement("visibility", "0"));
				ffe.addContent(fData);
				fe.addContent(ffe);
			}
		}
		
		// Create the network link entry
		Element nle = new Element("NetworkLink");
		nle.addContent(XMLUtils.createElement("name", SystemData.get("airline.name") + " ACARS Live Map"));
		Element nlu = new Element("Url");
		nlu.addContent(XMLUtils.createElement("href", ctx.getRequest().getRequestURL().toString()));
		nlu.addContent(XMLUtils.createElement("refreshMode", "onInterval"));
		nlu.addContent(XMLUtils.createElement("refreshInterval", "30"));
		nlu.addContent(XMLUtils.createElement("viewRefreshMode", "never"));
		nle.addContent(nlu);
		de.addContent(nle);
		
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