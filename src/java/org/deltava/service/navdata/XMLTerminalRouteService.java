// Copyright 2007, 2008, 2009, 2012, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.text.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom2.*;

import org.deltava.beans.navdata.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.XMLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display Terminal Route data to ACARS clients.
 * @author Luke
 * @version 6.1
 * @since 2.4
 */

public class XMLTerminalRouteService extends DownloadService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Ensure we are a dispatcher
		if (!ctx.isUserInRole("Pilot"))
			throw error(SC_UNAUTHORIZED, "Not in Pilot role", false);
		
		// Check if the file exists
		File cacheDir = new File(SystemData.get("schedule.cache"));
		File f = new File(cacheDir, "sidstar.zip");
		long age = f.exists() ? (System.currentTimeMillis() - f.lastModified()) : Long.MAX_VALUE;

		// Check the cache
		if ((age < 3600_000) && (f.length() > 10240)) {
			ctx.setHeader("Content-disposition", "attachment; filename=xmlsidstar.zip");
			ctx.setContentType("application/zip");
			ctx.setHeader("max-age", 1800);
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

		// Format routes
		int apCount = 0;
		final NumberFormat df = new DecimalFormat("#0.000000");
		try {
			Document doc = null;
			String lastAirport = null;
			Element re = new Element("routes");
			try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(f))) {
				try (PrintWriter pw = new PrintWriter(zout)) {
					for (Iterator<TerminalRoute> i = routes.iterator(); i.hasNext();) {
						TerminalRoute tr = i.next();
						boolean isNewAirport = !tr.getICAO().equals(lastAirport);
						if (isNewAirport) {
							if (doc != null) {
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
							apCount++;
						}

						// Create the SID/STAR element
						Element tre = new Element(tr.getType().name().toLowerCase());
						tre.setAttribute("name", tr.getName());
						tre.setAttribute("id", tr.getCode());
						tre.setAttribute("transition", tr.getTransition());
						tre.setAttribute("runway", tr.getRunway());
						re.addContent(tre);

						// Add the waypoint elements
						int mrkID = 0;
						for (NavigationDataBean ai : tr.getWaypoints()) {
							Element we = new Element("wp");
							we.setAttribute("code", ai.getCode());
							we.setAttribute("idx", String.valueOf(++mrkID));
							we.setAttribute("lat", df.format(ai.getLatitude()));
							we.setAttribute("lon", df.format(ai.getLongitude()));
							we.setAttribute("type", ai.getType().getName());
							if (ai.getRegion() != null)
								we.setAttribute("region", ai.getRegion());
							tre.addContent(we);
						}

						i.remove();
					}

					// Write the last entry
					if (doc != null) {
						pw.println(XMLUtils.format(doc, "UTF-8"));
						pw.flush();
					}
				}
			}

			// Format and write
			ctx.setHeader("Content-disposition", "attachment; filename=xmlsidstar.zip");
			ctx.setContentType("application/zip");
			ctx.setExpiry(1800);
			ctx.setHeader("airportCount", apCount);
			sendFile(f, ctx.getResponse());
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error", false);
		}

		return SC_OK;
	}

	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE always
	 */
	@Override
	public boolean isSecure() {
		return true;
	}
}