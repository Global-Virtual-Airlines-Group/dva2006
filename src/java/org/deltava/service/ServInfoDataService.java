// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.deltava.beans.DateTime;
import org.deltava.beans.TZInfo;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.GetPilotOnline;
import org.deltava.dao.DAOException;
import org.deltava.dao.http.GetServInfo;

import org.deltava.util.system.SystemData;

/**
 * A Web Service to display Airline users on VATSIM/IVAO for ServInfo.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ServInfoDataService extends WebDataService {

	private static final Logger log = Logger.getLogger(ServInfoDataService.class);

	// Date formatter for validity date
	private static final DateFormat _df = new SimpleDateFormat("yyyyMMddHHmmss");

	// Networks
	public static final String[] NETWORKS = { "VATSIM", "IVAO" };

	/**
	 * Executes the Web Service, returning a ServInfo data feed.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the Pilots and their network IDs
		Map pilots = new HashMap();
		try {
			GetPilotOnline dao = new GetPilotOnline(_con);
			for (int x = 0; x < NETWORKS.length; x++)
				pilots.putAll(dao.getIDs(NETWORKS[x]));
		} catch (DAOException de) {
			throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}

		// Load network data (this will use the cached copy if still valid)
		Collection users = new ArrayList();
		try {
			for (int x = 0; x < NETWORKS.length; x++) {
				// Get network status
				HttpURLConnection con = getURL(SystemData.get("online." + NETWORKS[x].toLowerCase() + ".status_url"));
				GetServInfo sdao = new GetServInfo(con);
				NetworkStatus status = sdao.getStatus(NETWORKS[x]);
				con.disconnect();

				// Get network info
				con = getURL(status.getDataURL());
				GetServInfo idao = new GetServInfo(con);
				NetworkInfo info = idao.getInfo(NETWORKS[x]);

				// Mash the VATSIM/IVAO user data together
				users.addAll(combineUsers(info, pilots));
			}
		} catch (Exception e) {
			throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}

		// Get the current date/time in UTC
		DateTime dt = new DateTime(new Date(), TZInfo.local());

		// Set the content type
		ctx.getResponse().setContentType("text/plain");
		ctx.getResponse().setBufferSize(16384);

		// Write the servinfo data
		try {
			PrintWriter pw = ctx.getResponse().getWriter();
			pw.println("; " + SystemData.get("airline.name") + " Online Pilot ServInfo data feed");
			pw.println(";");

			// GENERAL section
			pw.println("!GENERAL");
			pw.println("VERSION = 8");
			pw.println("RELOAD = 5");
			pw.println("UPDATE = " + _df.format(dt.getUTC()));
			pw.println("CONNECTED CLIENTS = " + users.size());
			pw.println(";");

			// CLIENTS section
			pw.println("!CLIENTS");
			for (Iterator i = users.iterator(); i.hasNext();) {
				Pilot p = (Pilot) i.next();
				pw.println(p.getRawData());
			}

			pw.println(";");

			// Other Sections
			pw.println("!SERVERS");
			pw.println(";");
			pw.println("!VOICE SERVERS");
			pw.println(";");
			pw.println("!PREFILE");
			pw.println(";");
			pw.println("; END");
			pw.println(";");
			ctx.getResponse().flushBuffer();
		} catch (IOException ie) {
			throw new ServiceException(HttpServletResponse.SC_CONFLICT, "I/O Error");
		}

		// Return result code
		return HttpServletResponse.SC_OK;
	}

	/**
	 * Helper method to open a connection to a particular URL.
	 */
	private HttpURLConnection getURL(String dataURL) throws IOException {
		URL url = new URL(dataURL);
		log.info("Loading data from " + url.toString());
		return (HttpURLConnection) url.openConnection();
	}

	/**
	 * Helper method to extract airline members from ServInfo data.
	 */
	private Collection combineUsers(NetworkInfo info, Map pilots) {

		List results = new ArrayList();
		for (int x = 0; x < NETWORKS.length; x++) {
			for (Iterator i = info.getPilots().iterator(); i.hasNext();) {
				Pilot p = (Pilot) i.next();
				if (pilots.containsKey(String.valueOf(p.getID())))
					results.add(p);
			}
		}

		return results;
	}
}