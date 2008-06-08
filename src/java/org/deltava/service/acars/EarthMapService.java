// Copyright 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.util.zip.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.log4j.Logger;

import org.jdom.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.UserDataMap;
import org.deltava.beans.acars.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;
import org.deltava.util.cache.*;

import org.gvagroup.acars.ACARSAdminInfo;
import org.gvagroup.common.SharedData;

/**
 * A Web Service to render the ACARS Map in Google Earth.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class EarthMapService extends GoogleEarthService {
	
	private static final Logger log = Logger.getLogger(EarthMapService.class);
	
	private final Cache<CacheableList<RouteEntry>> _cache = new ExpiringCache<CacheableList<RouteEntry>>(1, 5);
	private final Cache<CacheableSet<Integer>> _idCache = new ExpiringCache<CacheableSet<Integer>>(1, 5);

	/**
	 * Executes the Web Service, writing ACARS flight data in KML format.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the ACARS connection data
		CacheableList<RouteEntry> entries = _cache.get(MapService.class);
		CacheableSet<Integer> ids = _idCache.get(MapService.class);
		synchronized (_cache) {
			if ((entries == null) || (ids == null)) {
				entries = new CacheableList<RouteEntry>(MapService.class);
				ids = new CacheableSet<Integer>(MapService.class);
			
				// Get the pool
				ACARSAdminInfo<RouteEntry> acarsPool = (ACARSAdminInfo) SharedData.get(SharedData.ACARS_POOL);
				if (acarsPool == null)
					return SC_NOT_FOUND;
			
				entries.addAll(IPCUtils.deserialize(acarsPool));
				ids.addAll(acarsPool.getFlightIDs());
				_cache.add(entries);
				_idCache.add(ids);
			}
		}
		
		// Get the entries
		Map positions = CollectionUtils.createMap(entries, "ID");

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
		
		// Determine if we compress the KML or not
		boolean noCompress = Boolean.valueOf(ctx.getParameter("noCompress")).booleanValue();
		try {
			if (noCompress) {
				ctx.getResponse().setContentType("application/vnd.google-earth.kml+xml");
				ctx.getResponse().setHeader("Content-disposition", "attachment; filename=acarsMap.kml");
				ctx.print(XMLUtils.format(doc, "UTF-8"));
			} else {
				ctx.getResponse().setContentType("application/vnd.google-earth.kmz");
				ctx.getResponse().setHeader("Content-disposition", "attachment; filename=acarsMap.kmz");

				// Create the ZIP output stream
				ZipOutputStream zout = new ZipOutputStream(ctx.getResponse().getOutputStream());
				zout.putNextEntry(new ZipEntry("acarsMap.kml"));
				zout.write(XMLUtils.format(doc, "UTF-8").getBytes("UTF-8"));
				zout.closeEntry();
				zout.close();
			}

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