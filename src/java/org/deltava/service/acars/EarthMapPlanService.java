// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.util.zip.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.apache.log4j.Logger;

import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;

import org.gvagroup.acars.ACARSAdminInfo;
import org.gvagroup.common.SharedData;

/**
 * A Web Service to display ACARS flight plan data in Google Earth.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EarthMapPlanService extends GoogleEarthService {
	
	private static final Logger log = Logger.getLogger(EarthMapPlanService.class);

	/**
	 * Executes the Web Service, writing ACARS flight data in KML format.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the ACARS flights currently in progress
		ACARSAdminInfo acarsPool = (ACARSAdminInfo) SharedData.get(SharedData.ACARS_POOL);
		if (acarsPool == null)
			return SC_NOT_FOUND;

		// Load the flight information
		Collection<Integer> ids = acarsPool.getFlightIDs();
		Collection<FlightInfo> flights = new TreeSet<FlightInfo>();
		try {
			Connection con = ctx.getConnection();
			GetACARSData dao = new GetACARSData(con);
			GetNavData navdao = new GetNavData(con);

			// Loop through the flights
			for (Iterator<Integer> i = ids.iterator(); i.hasNext(); ) {
				int flightID = i.next().intValue();
				FlightInfo info = dao.getInfo(flightID);
				if (info != null) {
					Collection<String> routeEntries = new LinkedHashSet<String>();
					if (info.getSID() != null)
						routeEntries.addAll(info.getSID().getWaypoints());
					routeEntries.addAll(StringUtils.split(info.getRoute(), " "));
					if (info.getSTAR() != null)
						routeEntries.addAll(info.getSTAR().getWaypoints());
					
					// Load the navaids
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
					flights.add(info);
				}
			}
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
			return SC_INTERNAL_SERVER_ERROR;
		} finally {
			ctx.release();
		}
		
		// Build the XML document
		Document doc = new Document();
		Element ke = new Element("kml");
		doc.setRootElement(ke);
		Element de = new Element("Document");
		ke.addContent(de);

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
			ctx.getResponse().setHeader("Content-disposition", "attachment; filename=acarsMapPlans.kmz");
			ctx.getResponse().setContentType("application/vnd.google-earth.kmz kmz");
			
			// Create the ZIP output stream
			ZipOutputStream zout = new ZipOutputStream(ctx.getResponse().getOutputStream());
			zout.putNextEntry(new ZipEntry("acarsMapPlans.kml"));
			zout.write(XMLUtils.format(doc, "ISO-8859-1").getBytes("ISO-8859-1"));
			zout.closeEntry();
			zout.close();

			// Flush the buffer
			ctx.getResponse().flushBuffer();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error");
		}

		// Return success code
		return SC_OK;
	}
}