// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.deltava.beans.DateTime;
import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.TZInfo;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.GetPilotOnline;
import org.deltava.dao.DAOException;
import org.deltava.dao.file.GetServInfo;

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
	public static final String[] NETWORKS = { OnlineNetwork.VATSIM, OnlineNetwork.IVAO };

	/**
	 * Executes the Web Service, returning a ServInfo data feed.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the Pilots and their network IDs
		Map<String, Integer> pilots = new HashMap<String, Integer>();
		try {
			GetPilotOnline dao = new GetPilotOnline(_con);
			for (int x = 0; x < NETWORKS.length; x++)
				pilots.putAll(dao.getIDs(NETWORKS[x]));
		} catch (DAOException de) {
			throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}

		// Load network data (this will use the cached copy if still valid)
		Collection<Pilot> users = new ArrayList<Pilot>();
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

		// ServInfo header
		ctx.println("; " + SystemData.get("airline.name") + " Online Pilot ServInfo data feed");
		ctx.println(";");
		
		// GENERAL section
		ctx.println("!GENERAL");
		ctx.println("VERSION = 8");
		ctx.println("RELOAD = 5");
		ctx.println("UPDATE = " + _df.format(dt.getUTC()));
		ctx.println("CONNECTED CLIENTS = " + users.size());
		ctx.println(";");
		
		// CLIENTS section
		ctx.println("!CLIENTS");
		for (Iterator i = users.iterator(); i.hasNext();) {
			Pilot p = (Pilot) i.next();
			ctx.println(p.getRawData());
		}

		ctx.println(";");
		
		// Other Sections
		ctx.println("!SERVERS");
		ctx.println(";");
		ctx.println("!VOICE SERVERS");
		ctx.println(";");
		ctx.println("!PREFILE");
		ctx.println(";");
		ctx.println("; END");
		ctx.println(";");

		// Write the servinfo data
		try {
			ctx.getResponse().setContentType("text/plain");
			ctx.commit();
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
		log.debug("Loading data from " + url.toString());
		return (HttpURLConnection) url.openConnection();
	}

	/**
	 * Helper method to extract airline members from ServInfo data.
	 */
	private Collection<Pilot> combineUsers(NetworkInfo info, Map pilots) {

		List<Pilot> results = new ArrayList<Pilot>();
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