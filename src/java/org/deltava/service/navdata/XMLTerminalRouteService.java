// Copyright 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.text.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.navdata.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.XMLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display Terminal Route data to ACARS clients.
 * @author Luke
 * @version 2.4
 * @since 2.4
 */

public class XMLTerminalRouteService extends DispatchDataService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Ensure we are a dispatcher
		if (!ctx.isUserInRole("Pilot"))
			throw error(SC_UNAUTHORIZED, "Not in Pilot role", false);
		
		// Check the cache
		File f = _dataCache.get("XMLZIPSIDSTAR");
		if (f != null) {
			ctx.getResponse().setHeader("Content-disposition", "attachment; filename=xmlsidstar.zip");
			ctx.getResponse().setContentType("application/zip");
			ctx.getResponse().setIntHeader("max-age", 600);
			sendFile(f, ctx.getResponse());
			return SC_OK;
		}
		
		// Get the DAO and the SIDs/STARs
		Collection<TerminalRoute> routes = null;
		try {
			GetNavRoute navdao = new GetNavRoute(ctx.getConnection());
			routes = navdao.getAll();
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Write to a temp file
		File cacheDir = new File(SystemData.get("schedule.cache"));
		f = new File(cacheDir, "sidstar.zip");
		
		// Format routes
		final NumberFormat df = new DecimalFormat("#0.000000"); 
		try {
			Document doc = null; String lastAirport = null;
			Element re = new Element("routes");
			ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(f));
			for (Iterator<TerminalRoute> i = routes.iterator(); i.hasNext(); ) {
				TerminalRoute tr = i.next();
				boolean isNewAirport = !tr.getICAO().equals(lastAirport);
				if (isNewAirport) {
					if (doc != null) {
						PrintWriter pw = new PrintWriter(zout);
						pw.println(XMLUtils.format(doc, "UTF-8"));
						pw.flush();
					}
					
					lastAirport = tr.getICAO();
					ZipEntry ze = new ZipEntry("ss_" + tr.getICAO() + ".xml");
					ze.setMethod(ZipEntry.DEFLATED);
					doc = new Document();
					re = new Element("routes");
					doc.setRootElement(re);
					zout.putNextEntry(ze);
				}
				
				// Create the SID/STAR element
				Element tre = new Element(tr.getTypeName().toLowerCase());
				tre.setAttribute("name", tr.getName());
				tre.setAttribute("id", tr.getCode());
				tre.setAttribute("transition", tr.getTransition());
				tre.setAttribute("runway", tr.getRunway());
				re.addContent(tre);
				
				// Add the waypoint elements
				int mrkID = 0; 
				for (Iterator<NavigationDataBean> ii = tr.getWaypoints().iterator(); ii.hasNext(); ) {
					NavigationDataBean ai = ii.next();
					Element we = new Element("wp");
					we.setAttribute("code", ai.getCode());
					we.setAttribute("idx", String.valueOf(++mrkID));
					we.setAttribute("lat", df.format(ai.getLatitude()));
					we.setAttribute("lon", df.format(ai.getLongitude()));
					we.setAttribute("type", ai.getTypeName());
					if (ai.getRegion() != null)
						we.setAttribute("region", ai.getRegion());
					tre.addContent(we);
				}
				
				i.remove();
			}
			
			// Write the last entry
			if (doc != null) {
				PrintWriter pw = new PrintWriter(zout);
				pw.println(XMLUtils.format(doc, "UTF-8"));
				pw.flush();
			}
			
			// Close the file and add to the cache
			zout.close();
			addCacheEntry("XMLZIPSIDSTAR", f);
		
			// Format and write
			ctx.getResponse().setHeader("Content-disposition", "attachment; filename=xmlsidstar.zip");
			ctx.getResponse().setContentType("application/zip");
			ctx.getResponse().setIntHeader("max-age", 600);
			sendFile(f, ctx.getResponse());
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		// Write success code
		return SC_OK;
	}
	
	/**
	 * Returns wether this web service requires authentication.
	 * @return TRUE always
	 */
	public boolean isSecure() {
		return true;
	}
}