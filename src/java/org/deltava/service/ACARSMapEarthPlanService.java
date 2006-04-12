// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.apache.log4j.Logger;

import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display ACARS flight plan data in Google Earth.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ACARSMapEarthPlanService extends GoogleEarthService {
	
	private static final Logger log = Logger.getLogger(ACARSMapEarthPlanService.class);

	/**
	 * Executes the Web Service, writing ACARS flight data in KML format.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the ACARS flights currently in progress
		ACARSAdminInfo acarsPool = (ACARSAdminInfo) SystemData.getObject(SystemData.ACARS_POOL);
		Collection<Integer> ids = acarsPool.getFlightIDs();

		// Load the flight information
		Collection<FlightInfo> flights = new TreeSet<FlightInfo>();
		try {
			GetACARSData dao = new GetACARSData(_con);
			GetNavData navdao = new GetNavData(_con);

			// Loop through the flights
			for (Iterator<Integer> i = ids.iterator(); i.hasNext(); ) {
				int flightID = i.next().intValue();
				FlightInfo info = dao.getInfo(flightID);
				if (info != null) {
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

		// Add the NetworkLinkControl entry
		//ke.addContent(XMLUtils.createElement("NetworkLinkControl", "minRefreshPeriod", "300"));
		
		// Convert the flight plan data to KML
		for (Iterator<FlightInfo> i = flights.iterator(); i.hasNext(); ) {
			FlightInfo info = i.next();
			if (info.hasPlanData()) {
				Element fre = createFlightRoute(info.getFlightCode(), info.getPlanData(), true);
				KMLUtils.setVisibility(fre, false);
				de.addContent(fre);
			}
		}
		
		// Write the XML
		try {
			ctx.getResponse().setHeader("Content-disposition", "attachment; filename=acarsMapPlans.kml");
			ctx.getResponse().setContentType("application/vnd.google-earth.kml+xml");
			ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
			ctx.commit();
		} catch (IOException ie) {
			throw new ServiceException(SC_CONFLICT, "I/O Error");
		}

		// Return success code
		return SC_OK;
	}
}