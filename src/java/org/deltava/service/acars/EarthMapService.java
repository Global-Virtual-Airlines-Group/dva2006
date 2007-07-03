// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.util.zip.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.log4j.Logger;

import org.jdom.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.acars.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.system.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

import org.gvagroup.acars.ACARSAdminInfo;
import org.gvagroup.common.SharedData;

/**
 * A Web Service to render the ACARS Map in Google Earth.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EarthMapService extends GoogleEarthService {
	
	private static final Logger log = Logger.getLogger(EarthMapService.class);

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
		Collection<Integer> ids = acarsPool.getFlightIDs();
		Map positions = CollectionUtils.createMap(acarsPool.getMapEntries(), "ID");

		// Load the flight information
		Map<Integer, Pilot> pilots = new HashMap<Integer, Pilot>();
		Collection<FlightInfo> flights = new TreeSet<FlightInfo>();
		try {
			Connection con = ctx.getConnection();
			
			// Loop through the flights
			GetACARSData dao = new GetACARSData(con);
			Collection<Integer> userIDs = new HashSet<Integer>();
			for (Iterator<Integer> i = ids.iterator(); i.hasNext(); ) {
				Integer flightID = i.next();
				FlightInfo info = dao.getInfo(flightID.intValue());
				if (info != null) {
					userIDs.add(new Integer(info.getPilotID()));
					Collection<RouteEntry> routeData = dao.getRouteEntries(flightID.intValue(), info.getArchived());
					info.setRouteData(routeData);
					if (positions.containsKey(flightID))
						info.setPosition((RouteEntry) positions.get(flightID));
					
					// Add the flight data
					flights.add(info);
				}
			}
			
			// Load the user data
			GetUserData uddao = new GetUserData(con);
			UserDataMap udmap = uddao.get(userIDs);
			
			// Add into the pilot map
			GetPilot pdao = new GetPilot(con);
			for (Iterator<String> i = udmap.getTableNames().iterator(); i.hasNext(); ) {
				String tableName = i.next();
				if (UserDataMap.isPilotTable(tableName))
					pilots.putAll(pdao.getByID(userIDs, tableName));
			}
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
			return SC_INTERNAL_SERVER_ERROR;
		} finally {
			ctx.release();
		}
		
		// Create the flights and routes folders
		Map<String, Element> folders = new LinkedHashMap<String, Element>();
		folders.put("positions", XMLUtils.createElement("Folder", "name", "Aircraft"));
		folders.put("progress", XMLUtils.createElement("Folder", "name", "Flight Progress"));
		folders.put("airports", XMLUtils.createElement("Folder", "name", "Airports"));
		
		// Convert the flight data to KML
		int colorOfs = -1;
		Collection<Airport> airports = new TreeSet<Airport>();
		for (Iterator<FlightInfo> i = flights.iterator(); i.hasNext(); ) {
			FlightInfo info = i.next();
			Pilot usr = pilots.get(new Integer(info.getPilotID()));
			
			// Increment the line color 
			colorOfs++;
			if (colorOfs >= COLORS.length)
				colorOfs = 0;
			
			// Save the airports
			airports.add(info.getAirportD());
			airports.add(info.getAirportA());
			
			// Add the position and flight progress
			if (info.hasRouteData()) {
				String usrName = (usr == null) ? info.getFlightCode() : usr.getName() + " (" + usr.getPilotCode() + ")";
				folders.get("positions").addContent(createAircraft(usrName, info.getPosition()));

				Element fpe = createProgress(info.getRouteData(), COLORS[colorOfs]);
				XMLUtils.setChildText(fpe, "name", usrName);
				folders.get("progress").addContent(fpe);
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
		
		// Write the XML in ZIP format
		try {
			ctx.getResponse().setHeader("Content-disposition", "attachment; filename=acarsMap.kmz");
			ctx.getResponse().setContentType("application/vnd.google-earth.kmz kmz");

			// Create the ZIP output stream
			ZipOutputStream zout = new ZipOutputStream(ctx.getResponse().getOutputStream());
			zout.putNextEntry(new ZipEntry("acarsMap.kml"));
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

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	public final boolean isLogged() {
		return false;
	}
}