// Copyright 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
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

import org.deltava.dao.*;
import org.deltava.dao.ipc.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display ACARS flight plan data in Google Earth.
 * @author Luke
 * @version 2.6
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
		
		// Load the flight information
		Collection<FlightInfo> flights = new TreeSet<FlightInfo>();
		try {
			Connection con = ctx.getConnection();
			GetACARSData dao = new GetACARSData(con);
			GetNavRoute navdao = new GetNavRoute(con);

			// Loop through the flights
			GetACARSPool acdao = new GetACARSPool();
			for (Iterator<Integer> i = acdao.getFlightIDs().iterator(); i.hasNext(); ) {
				int flightID = i.next().intValue();
				FlightInfo info = dao.getInfo(flightID);
				if (info != null) {
					Collection<NavigationDataBean> route = new LinkedHashSet<NavigationDataBean>();
					if (info.getRunwayD() != null)
						route.add(info.getRunwayD());
					if (info.getSID() != null)
						route.addAll(info.getSID().getWaypoints());

					// Load the navaids
					route.addAll(navdao.getRouteWaypoints(info.getRoute(), info.getAirportD()));

					if (info.getSTAR() != null)
						route.addAll(info.getSTAR().getWaypoints());
					if (info.getRunwayA() != null)
						route.add(info.getRunwayA());
					
					info.setPlanData(GeoUtils.stripDetours(route, 50));
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
		Document doc = KMLUtils.createKMLRoot();
		Element de = new Element("Document");
		doc.getRootElement().addContent(de);

		// Convert the flight plan data to KML
		for (Iterator<FlightInfo> i = flights.iterator(); i.hasNext(); ) {
			FlightInfo info = i.next();
			if (info.hasPlanData()) {
				Element fre = createFlightRoute(info.getFlightCode(), info.getPlanData(), true);
				KMLUtils.setVisibility(fre, false);
				de.addContent(fre);
			}
		}
		
		// Clean up the namespace
		KMLUtils.copyNamespace(doc);
		
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
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		// Return success code
		return SC_OK;
	}
}