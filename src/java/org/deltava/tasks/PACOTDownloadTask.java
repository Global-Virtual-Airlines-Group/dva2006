// Copyright 2006, 2007, 2009, 2010, 2011, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.net.*;
import java.util.*;
import java.io.IOException;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to download PACOT data.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class PACOTDownloadTask extends Task {

	/**
	 * Initializes the Scheduled Task.
	 */
	public PACOTDownloadTask() {
		super("PACOT Download", PACOTDownloadTask.class);
	}

	/**
	 * Executes the task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		try {
			URL url = new URL(SystemData.get("config.pacot.url"));
			
			// Build the oceanic route bean
			OceanicNOTAM or = new OceanicNOTAM(OceanicTrackInfo.Type.PACOT, Instant.now());
			or.setSource(url.getHost());
			
			// Load waypoint data, retry up to 3 times
			log.info("Loading PACOT track data from " + url.toString());
			GetPACOTs dao = new GetPACOTs(url.toExternalForm());
			int retryCount = 0; boolean isDownloaded = false;
			while (!isDownloaded && (retryCount < 3)) {
				try {
					or.setRoute(dao.getTrackInfo());		
					isDownloaded = true;
				} catch (DAOException de) {
					dao.setConnectTimeout(5000);
					dao.reset();
					retryCount++;
					log.warn("Error downloading PACOT Data - " + de.getMessage());
				}
			}
			
			// Get the waypoint data
			Map<String, Collection<String>> trackData = dao.getWaypoints();
			log.info(trackData.keySet());
			Collection<String> waypointIDs = new HashSet<String>();
			for (Iterator<Collection<String>> i = trackData.values().iterator(); i.hasNext(); ) {
				Collection<String> waypoints = i.next();
				waypointIDs.addAll(waypoints);
			}
			
			// Get the intersection navdata values
			Connection con = ctx.getConnection();
			GetNavData nddao = new GetNavData(con);
			NavigationDataMap ndmap = nddao.getByID(waypointIDs);
			
			// Build the Route waypoints
			log.info("Building PACOT track waypoints");
			Collection<OceanicTrack> oTracks = new ArrayList<OceanicTrack>();
			for (Iterator<Map.Entry<String, Collection<String>>> i = trackData.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry<String, Collection<String>> e = i.next();
				OceanicTrack ot = new OceanicTrack(OceanicTrackInfo.Type.PACOT, e.getKey());
				ot.setDate(or.getDate());
				
				// Calculate the location of the waypoint
				GeoLocation lastLoc = new GeoPosition(35, -175);
				for (Iterator<String> wi = e.getValue().iterator(); wi.hasNext(); ) {
					String code = wi.next();
					NavigationDataBean ndb = ndmap.get(code, lastLoc);
					if (ndb != null) {
						NavigationDataBean nd = NavigationDataBean.create(ndb.getType(), ndb.getLatitude(), ndb.getLongitude());
						nd.setCode(ndb.getCode());
						nd.setRegion(ndb.getRegion());
						ot.addWaypoint(nd);
						lastLoc = ndb;
					}
				}

				oTracks.add(ot);
			}

			// Start a transaction
			ctx.startTX();
			
			// Write the route data to the database
			SetOceanic wdao = new SetOceanic(con);
			wdao.write(or);
			for (Iterator<OceanicTrack> i = oTracks.iterator(); i.hasNext(); )
				wdao.write(i.next());
			
			ctx.commitTX();
		} catch (IOException ie) {
			log.error("Error downloading PACOT Tracks - " + ie.getMessage(), ie);
		} catch (Exception e) {
			ctx.rollbackTX();
			log.error("Error saving PACOT Data - " + e.getMessage(), e);
		} finally {
			ctx.release();
		}
	}
}