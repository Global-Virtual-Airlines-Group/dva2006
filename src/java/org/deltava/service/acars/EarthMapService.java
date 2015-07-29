// Copyright 2006, 2007, 2008, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.util.zip.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.log4j.Logger;

import org.jdom2.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.UserDataMap;
import org.deltava.beans.acars.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.ipc.GetACARSPool;

import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to render the ACARS Map in Google Earth.
 * @author Luke
 * @version 4.2
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
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the ACARS connection data
		GetACARSPool acdao = new GetACARSPool();
		Collection<ACARSMapEntry> entries = acdao.getEntries();
		Collection<Integer> ids = acdao.getFlightIDs();
		
		// Get the entries
		Map<Integer, ACARSMapEntry> positions = CollectionUtils.createMap(entries, "ID");

		// Load the flight information
		Map<Integer, Pilot> pilots = new HashMap<Integer, Pilot>();
		Collection<FlightInfo> flights = new TreeSet<FlightInfo>();
		try {
			Connection con = ctx.getConnection();
			
			// Loop through the flights
			GetACARSPositions dao = new GetACARSPositions(con);
			Collection<Integer> userIDs = new HashSet<Integer>();
			for (Iterator<Integer> i = ids.iterator(); i.hasNext(); ) {
				Integer flightID = i.next();
				FlightInfo info = dao.getInfo(flightID.intValue());
				if (info != null) {
					userIDs.add(Integer.valueOf(info.getAuthorID()));
					Collection<? extends RouteEntry> routeData = dao.getRouteEntries(flightID.intValue(), info.getArchived());
					info.setRouteData(routeData);
					if (positions.containsKey(flightID))
						info.setPosition((RouteEntry) positions.get(flightID));
					
					flights.add(info);
				}
			}
			
			// Load the user data
			GetUserData uddao = new GetUserData(con);
			UserDataMap udmap = uddao.get(userIDs);
			
			// Add into the pilot map
			GetPilot pdao = new GetPilot(con);
			pilots.putAll(pdao.get(udmap));
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
			Pilot usr = pilots.get(Integer.valueOf(info.getAuthorID()));
			
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
		Document doc = KMLUtils.createKMLRoot();
		Element de = new Element("Document");
		doc.getRootElement().addContent(de);
		
		// Add the folders to the document
		for (Iterator<Element> i = folders.values().iterator(); i.hasNext(); ) {
			Element fe = i.next();
			de.addContent(fe);
		}
		
		// Clean up the namespace
		KMLUtils.copyNamespace(doc);
		
		// Determine if we compress the KML or not
		boolean noCompress = Boolean.valueOf(ctx.getParameter("noCompress")).booleanValue();
		try {
			if (noCompress) {
				ctx.setContentType("application/vnd.google-earth.kml+xml", "utf-8");
				ctx.setHeader("Content-disposition", "attachment; filename=acarsMap.kml");
				ctx.print(XMLUtils.format(doc, "UTF-8"));
				ctx.commit();
			} else {
				ctx.setContentType("application/vnd.google-earth.kmz");
				ctx.setHeader("Content-disposition", "attachment; filename=acarsMap.kmz");

				// Create the ZIP output stream
				try (ZipOutputStream zout = new ZipOutputStream(ctx.getResponse().getOutputStream())) {
					zout.putNextEntry(new ZipEntry("acarsMap.kml"));
					zout.write(XMLUtils.format(doc, "UTF-8").getBytes("UTF-8"));
					zout.closeEntry();
				}
			}

			// Flush the buffer
			ctx.getResponse().flushBuffer();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	@Override
	public final boolean isLogged() {
		return false;
	}
}