// Copyright 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.stats.PilotLocation;

import org.deltava.dao.*;
import org.deltava.util.*;
import org.deltava.util.cache.*;

/**
 * A Web Service to display Pilot Locations on a map.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class PilotLocationService extends WebService {

	private static final Cache<CacheableSet<Element>> _cache = new ExpiringCache<CacheableSet<Element>>(1, 3600);

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Check if we have any cached data
		CacheableSet<Element> locs = null;
		synchronized (PilotLocationService.class) {
			locs = _cache.get(PilotLocationService.class);
			if (locs == null) {
				// Calculate the random location adjuster (between -1.5 and +1.5)
				Random rnd = new Random();
				double rndAmt = ((rnd.nextDouble() * 3) - 1) / GeoLocation.DEGREE_MILES;

				// Get active pilots and their locations
				Collection<Pilot> pilots = null;
				Map<Integer, GeoLocation> locations = null;
				try {
					GetPilot dao = new GetPilot(ctx.getConnection());
					locations = dao.getPilotBoard();
					pilots = dao.getActivePilots(null);
				} catch (DAOException de) {
					throw new ServiceException(SC_INTERNAL_SERVER_ERROR, de.getMessage());
				} finally {
					ctx.release();
				}

				// Loop through the GeoLocations, apply the random adjuster and combine with the Pilot
				Collection<PilotLocation> usrs = new LinkedHashSet<PilotLocation>();
				for (Iterator<Pilot> i = pilots.iterator(); i.hasNext();) {
					Pilot usr = i.next();
					GeoLocation gl = locations.get(new Integer(usr.getID()));
					if (gl != null) {
						GeoPosition gp = new GeoPosition(gl);
						gp.setLatitude(gp.getLatitude() + rndAmt);
						gp.setLongitude(gp.getLongitude() + rndAmt);

						// Build the pilot location and calculate the minimum zoom
						PilotLocation loc = new PilotLocation(usr, gp);
						int distance = 8192;
						int minZoom = 1;
						Collection<GeoLocation> neighbors = GeoUtils.neighbors(loc, usrs, distance);
						while ((neighbors.size() > 60) && (minZoom < 12) && (distance > 30)) {
							distance /= 1.9;
							minZoom++;
							neighbors = GeoUtils.neighbors(loc, neighbors, distance);
						}

						loc.setMinZoom(minZoom);
						usrs.add(loc);
					}
				}
				
				// Add the Map entries
				locs = new CacheableSet<Element>(PilotLocationService.class);
				for (Iterator<PilotLocation> i = usrs.iterator(); i.hasNext();) {
					PilotLocation loc = i.next();
					Pilot usr = loc.getUser();
					Element e = XMLUtils.createElement("pilot", loc.getInfoBox(), true);
					e.setAttribute("rank", usr.getRank());
					e.setAttribute("eqType", usr.getEquipmentType());
					e.setAttribute("minZoom", String.valueOf(loc.getMinZoom()));
					e.setAttribute("lat", StringUtils.format(loc.getLatitude(), "##0.00000"));
					e.setAttribute("lng", StringUtils.format(loc.getLongitude(), "##0.00000"));
					e.setAttribute("color", loc.getIconColor());
					locs.add(e);
				}

				// Add to the cache
				_cache.add(locs);
			}
		}

		// Create the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Add the Map entries
		for (Iterator<Element> i = locs.iterator(); i.hasNext();) {
			Element e = i.next();
			re.addContent((Element) e.clone());
		}

		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.getResponse().setCharacterEncoding("UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		// Return success code
		return SC_OK;
	}

	/**
	 * Returns if the Web Service invocation is logged.
	 * @return FALSE
	 */
	public boolean isLogged() {
		return false;
	}
}