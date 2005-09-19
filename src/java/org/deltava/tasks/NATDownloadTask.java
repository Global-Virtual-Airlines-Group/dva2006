// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.tasks;

import java.net.*;
import java.util.Date;
import java.io.IOException;

import org.deltava.beans.schedule.OceanicRoute;

import org.deltava.dao.http.GetNATs;
import org.deltava.dao.SetRoute;
import org.deltava.dao.DAOException;

import org.deltava.taskman.DatabaseTask;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to download Oceanic Track data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NATDownloadTask extends DatabaseTask {

	/**
	 * Initializes the Scheduled Task.
	 */
	public NATDownloadTask() {
		super("NAT Download", NATDownloadTask.class);
	}

	/**
	 * Executes the task.
	 */
	protected void execute() {

		try {
			// Create the URL connection to the NAT Download side
			URL url = new URL(SystemData.get("config.nat"));
			log.info("Loading NAT track data from " + url.toString());
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			// Build the oceanic route bean
			OceanicRoute or = new OceanicRoute(OceanicRoute.NAT);
			or.setDate(new Date());
			or.setSource(url.getHost());

			// Get the DAO and the NAT data
			GetNATs dao = new GetNATs(con);
			or.setRoute(dao.getTrackInfo());

			// Write the route data to the database
			SetRoute wdao = new SetRoute(_con);
			wdao.write(or);
		} catch (IOException ie) {
			log.error("Error downloading NAT Tracks - " + ie.getMessage(), ie);
		} catch (DAOException de) {
			log.error("Error saving NAT Data - " + de.getMessage(), de);
		}
	}
}