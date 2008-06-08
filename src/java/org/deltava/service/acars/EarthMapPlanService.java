// Copyright 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 2.2
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
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the ACARS flights currently in progress
		ACARSAdminInfo acarsPool = (ACARSAdminInfo) SharedData.get(SharedData.ACARS_POOL);
		if (acarsPool == null)
			return SC_NOT_FOUND;

		// Load the flight information
		Collection ids = acarsPool.getFlightIDs();
		Collection<FlightInfo> flights = new TreeSet<FlightInfo>();
		try {
			Connection con = ctx.getConnection();
			GetACARSData dao = new GetACARSData(con);
			GetNavData navdao = new GetNavData(con);

			// Loop through the flights
			for (Iterator i = ids.iterator(); i.hasNext(); ) {
				int flightID = ((Integer) i.next()).intValue();
				FlightInfo info = dao.getInfo(flightID);
				if (info != null) {
					Collection<NavigationDataBean> route = new LinkedHashSet<NavigationDataBean>();
					if (info.getSID() != null)
						route.addAll(info.getSID().getWaypoints());

					// Load the navaids
					Collection<String> rPoints = StringUtils.split(info.getRoute(), " ");
					GeoPosition lastWaypoint = new GeoPosition(info.getAirportD());
					int distance = lastWaypoint.distanceTo(info.getAirportA());
					NavigationDataMap navaids = navdao.getByID(rPoints);

					// Filter out navaids and put them in the correct order
					for (Iterator<String> ri = rPoints.iterator(); ri.hasNext();) {
						String navCode = ri.next();
						NavigationDataBean wPoint = navaids.get(navCode, lastWaypoint);
						if (wPoint != null) {
							if (lastWaypoint.distanceTo(wPoint) < distance) {
								route.add(wPoint);
								lastWaypoint.setLatitude(wPoint.getLatitude());
								lastWaypoint.setLongitude(wPoint.getLongitude());
							}
						}
					}

					if (info.getSTAR() != null)
						route.addAll(info.getSTAR().getWaypoints());
					
					info.setPlanData(route);
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
			zout.write(XMLUtils.format(doc, "UTF-8").getBytes("UTF-8"));
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