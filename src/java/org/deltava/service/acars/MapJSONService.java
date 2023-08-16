// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2012, 2016, 2017, 2018, 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.io.*;
import java.time.Instant;
import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;

import org.deltava.dao.ipc.GetACARSPool;

import org.deltava.service.*;
import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to provide JSON-formatted ACARS position data for Google Maps.
 * @author Luke
 * @version 11.1
 * @since 1.0
 */

public class MapJSONService extends WebService {
	
	private static final Cache<CacheableMap<String, MapRouteEntry>> _simFDRFlightCache = CacheManager.getMap(String.class, MapRouteEntry.class, "simFDRFlightID");
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the pool data
		GetACARSPool acdao = new GetACARSPool();
		Collection<ACARSMapEntry> entries = acdao.getEntries();
		if (entries == null)
			entries = Collections.emptyList();
		
		// Get the simFDR tracks
		CacheableMap<String, MapRouteEntry> trackIDs = _simFDRFlightCache.get(MapRouteEntry.class);
		if (trackIDs == null)
			trackIDs = new CacheableMap<String, MapRouteEntry>(MapRouteEntry.class);
		
		// Trim out any old cache entries
		Instant purgeDate = Instant.now().minusSeconds(900); boolean entriesPurged = false;
		for (Iterator<Map.Entry<String, MapRouteEntry>> i = trackIDs.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<String, MapRouteEntry> me = i.next();
			if (me.getValue().getDate().isBefore(purgeDate)) {
				i.remove();
				entriesPurged = true;
			}
		}
		
		// Write back if we've updated the cache
		if (entriesPurged)
			_simFDRFlightCache.add(trackIDs);

		// Add the items, then prune from other virtual airlines
		entries.addAll(trackIDs.values());
		if (!ctx.isUserInRole("Developer")) {
			final String airlineCode = SystemData.get("airline.code");
			entries.removeIf(e -> !e.getPilot().getAirlineCode().equals(airlineCode));
		}
		
		JSONObject jo = new JSONObject();
		for (ACARSMapEntry entry : entries) {
			JSONObject eo = new JSONObject();
			eo.put("ll", JSONUtils.format(entry));
			eo.put("color", entry.getIconColor());
			eo.put("busy", entry.isBusy());
			
			// Display heading if available
			if (entry instanceof RouteEntry rte) {
				eo.put("hdg", rte.getHeading());
				eo.put("gs", rte.getGroundSpeed());
			}
			
			if (entry instanceof GroundMapEntry gme)
				eo.put("range", (gme.getRange() > 5000) ? 0 : gme.getRange());
			else if ((entry.getID() == 0) && (entry instanceof MapRouteEntry mrte))
				eo.put("external_id", mrte.getExternalID());
			else
				eo.put("flight_id", entry.getID());
			
			// Display icons as required
			if (entry instanceof IconMapEntry ime) {
				eo.put("pal", ime.getPaletteCode());
				eo.put("icon", ime.getIconCode());
			}
			
			// Add tabs
			if (entry instanceof TabbedMapEntry tme) {
				for (Map.Entry<String, String> me : tme.getTabs().entrySet()) {
					JSONObject to = new JSONObject();
					to.put("name", me.getKey());
					to.put("content", me.getValue());
					eo.append("tabs", to);
				}
			} else {
				eo.put("tabs", new JSONArray());
				eo.put("info", entry.getInfoBox());
			}
			
			// Add pilot name
			if (entry.getPilot() != null) {
				Pilot p = entry.getPilot();
				JSONObject po = new JSONObject();
				po.put("name", p.getName());
				po.put("id", p.getID());
				if (!StringUtils.isEmpty(p.getPilotCode())) {
					po.put("code", p.getPilotCode());
					po.put("pn", p.getPilotNumber());
				}

				eo.put("pilot", po);
			}
			
			jo.append(entry.getType().toString().toLowerCase(), eo);
		}

		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "aircraft", "dispatch");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(5);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	@Override
	public final boolean isLogged() {
		return false;
	}
}