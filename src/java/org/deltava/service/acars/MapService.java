// Copyright 2005, 2006, 2007, 2008, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.io.*;
import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;

import org.deltava.dao.ipc.GetACARSPool;

import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to provide XML-formatted ACARS position data for Google Maps.
 * @author Luke
 * @version 2.8
 * @since 1.0
 */

public class MapService extends WebService {
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the pool data
		GetACARSPool acdao = new GetACARSPool();
		Collection<ACARSMapEntry> entries = acdao.getEntries();

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Add the items
		for (Iterator<ACARSMapEntry> i = entries.iterator(); i.hasNext();) {
			ACARSMapEntry entry = i.next();
			Element e = new Element(entry.isDispatch() ? "dispatch" : "aircraft");
			e.setAttribute("lat", StringUtils.format(entry.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(entry.getLongitude(), "##0.00000"));
			e.setAttribute("color", entry.getIconColor());
			e.setAttribute("busy", String.valueOf(entry.isBusy()));
			
			if (entry.isDispatch()) {
				DispatchMapEntry dme = (DispatchMapEntry) entry;
				e.setAttribute("range", String.valueOf((dme.getRange() > 5000) ? 0 : dme.getRange()));
			} else
				e.setAttribute("flight_id", String.valueOf(entry.getID()));
			
			// Display icons as required
			if (entry instanceof IconMapEntry) {
				IconMapEntry ime = (IconMapEntry) entry;
				e.setAttribute("pal", String.valueOf(ime.getPaletteCode()));
				e.setAttribute("icon", String.valueOf(ime.getIconCode()));
			}
			
			// Add tabs
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
				e.addContent(XMLUtils.createElement("info", entry.getInfoBox(), true));
			}
			
			// Add pilot name
			if (entry.getPilot() != null) {
				Pilot p = entry.getPilot();
				Element pe = XMLUtils.createElement("pilot", p.getName(), true);
				if (!StringUtils.isEmpty(p.getPilotCode()))
					pe.setAttribute("id", p.getPilotCode());

				e.addContent(pe);
			}
			
			re.addContent(e);
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
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	public final boolean isLogged() {
		return false;
	}
}