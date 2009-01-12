// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.servinfo;

import java.io.*;
import java.util.*;
import java.text.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.DateTime;
import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.TZInfo;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.*;
import org.deltava.dao.file.GetServInfo;

import org.deltava.service.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display Airline users on VATSIM/IVAO for ServInfo.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class DataService extends WebService {

	// Networks
	public static final OnlineNetwork[] NETWORKS = { OnlineNetwork.VATSIM, OnlineNetwork.IVAO };

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
			GetPilotOnline dao = new GetPilotOnline(ctx.getConnection());
			for (int x = 0; x < NETWORKS.length; x++)
				pilots.putAll(dao.getIDs(NETWORKS[x]));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Load network data (this will use the cached copy if still valid)
		Collection<Pilot> users = new ArrayList<Pilot>();
		try {
			for (int x = 0; x < NETWORKS.length; x++) {
				File f = new File(SystemData.get("online." + NETWORKS[x].toString().toLowerCase() + ".local.info"));
				if (f.exists()) {
					GetServInfo sidao = new GetServInfo(new FileInputStream(f));
					NetworkInfo info = sidao.getInfo(NETWORKS[x]);
					if (info != null)
						users.addAll(combineUsers(info, pilots));
				}
			}
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}

		// Get the current date/time in UTC
		DateTime dt = new DateTime(new Date(), TZInfo.local());

		// ServInfo header
		ctx.println("; " + SystemData.get("airline.name") + " Online Pilot ServInfo data feed");
		ctx.println(";");
		
		// GENERAL section
		final DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		ctx.println("!GENERAL");
		ctx.println("VERSION = 8");
		ctx.println("RELOAD = 5");
		ctx.println("UPDATE = " + df.format(dt.getUTC()));
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
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		// Return result code
		return SC_OK;
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