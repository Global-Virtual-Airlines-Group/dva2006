// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.io.*;
import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;

import org.deltava.dao.ipc.GetACARSPool;

import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to provide JSON-formatted ACARS position data for Google Maps.
 * @author Luke
 * @version 7.3
 * @since 1.0
 */

public class MapJSONService extends WebService {
	
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

		// Add the items
		JSONObject jo = new JSONObject();
		for (ACARSMapEntry entry : entries) {
			JSONObject eo = new JSONObject();
			eo.put("ll", JSONUtils.format(entry));
			eo.put("color", entry.getIconColor());
			eo.put("busy", entry.isBusy());
			
			// Display heading if available
			if (entry instanceof RouteEntry) {
				RouteEntry rte = (RouteEntry) entry;
				eo.put("hdg", rte.getHeading());
				eo.put("gs", rte.getGroundSpeed());
			}
			
			if (entry instanceof GroundMapEntry) {
				GroundMapEntry gme = (GroundMapEntry) entry;
				eo.put("range", (gme.getRange() > 5000) ? 0 : gme.getRange());
			} else
				eo.put("flight_id", entry.getID());
			
			// Display icons as required
			if (entry instanceof IconMapEntry) {
				IconMapEntry ime = (IconMapEntry) entry;
				eo.put("pal", ime.getPaletteCode());
				eo.put("icon", ime.getIconCode());
			}
			
			// Add tabs
			if (entry instanceof TabbedMapEntry) {
				TabbedMapEntry tme = (TabbedMapEntry) entry;
				for (int x = 0; x < tme.getTabNames().size(); x++) {
					JSONObject to = new JSONObject();
					to.put("name", tme.getTabNames().get(x));
					to.put("content", tme.getTabContents().get(x));
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
				if (!StringUtils.isEmpty(p.getPilotCode()))
					po.put("code", p.getPilotCode());

				eo.put("pilot", po);
			}
			
			jo.append(entry.getType().toString().toLowerCase(), eo);
		}

		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "aircraft", "dispatch");
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(5);
			ctx.println(jo.toString());
			ctx.commit();
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