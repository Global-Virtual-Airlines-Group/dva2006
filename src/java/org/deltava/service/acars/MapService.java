// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.io.*;
import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.service.*;
import org.deltava.util.*;
import org.deltava.util.cache.*;

import org.gvagroup.acars.ACARSAdminInfo;
import org.gvagroup.common.SharedData;

/**
 * A Web Service to provide XML-formatted ACARS position data for Google Maps.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MapService extends WebService {
	
	private final Cache<CacheableList<RouteEntry>> _cache = new ExpiringCache<CacheableList<RouteEntry>>(1, 4); 

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the ACARS connection data
		CacheableList<RouteEntry> entries = _cache.get(MapService.class);
		synchronized (_cache) {
			if (entries == null) {
				entries = new CacheableList<RouteEntry>(MapService.class);
			
				// Get the pool
				ACARSAdminInfo<RouteEntry> acarsPool = (ACARSAdminInfo) SharedData.get(SharedData.ACARS_POOL);
				if (acarsPool == null)
					return SC_NOT_FOUND;
			
				entries.addAll(IPCUtils.deserialize(acarsPool));
				_cache.add(entries);
			}
		}

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Add the items
		for (Iterator<RouteEntry> i = entries.iterator(); i.hasNext();) {
			RouteEntry entry = i.next();
			Element e = new Element("aircraft");
			e.setAttribute("lat", StringUtils.format(entry.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(entry.getLongitude(), "##0.00000"));
			e.setAttribute("flight_id", String.valueOf(entry.getID()));
			e.setAttribute("color", entry.getIconColor());
			if (entry instanceof TabbedMapEntry) {
				TabbedMapEntry tme = (TabbedMapEntry) entry;
				e.setAttribute("tabs", String.valueOf(tme.getTabNames().size()));
				for (int x = 0; x < tme.getTabNames().size(); x++) {
					Element te = new Element("tab");
					te.setAttribute("name", tme.getTabNames().get(x));
					te.addContent(new CDATA(tme.getTabContents().get(x)));
					e.addContent(te);
				}
			} else {
				e.setAttribute("tabs", "0");
				e.addContent(new CDATA(entry.getInfoBox()));
			}

			re.addContent(e);
		}

		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
			ctx.commit();
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