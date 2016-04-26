// Copyright 2006, 2007, 2009, 2010, 2011, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
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
 * A Scheduled Task to download AUSOT data.
 * @author Luke
 * @version 7.0
 * @since 2.7
 */

public class AUSOTDownloadTask extends Task {

	/**
	 * Initializes the Scheduled Task.
	 */
	public AUSOTDownloadTask() {
		super("AUSOT Download", AUSOTDownloadTask.class);
	}

	/**
	 * Executes the task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		try {
			
			// Create the URL connection to the PACOT Download side
			URL url = new URL(SystemData.get("config.ausot.url"));
			
			// Build the oceanic route bean
			OceanicNOTAM or = new OceanicNOTAM(OceanicTrackInfo.Type.AUSOT, Instant.now());
			or.setSource(url.getHost());
			
			// Get the DAO and the AUSOT data
			log.info("Loading AUSOT track data from " + url.toString());
			GetAUSOTs dao = new GetAUSOTs(url.toExternalForm());
			
			// Load waypoint data, retry up to 3 times
			int retryCount = 0; boolean isDownloaded = false;
			while (!isDownloaded && (retryCount < 3)) {
				try {
					or.setRoute(dao.getTrackInfo());		
					isDownloaded = true;
				} catch (DAOException de) {
					dao.setConnectTimeout(5000);
					dao.reset();
					retryCount++;
					log.warn("Error downloading AUSOT Data - " + de.getMessage());
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
			GeoPosition ctr = new GeoPosition(-26, 133);
			
			// Build the Route waypoints
			log.info("Building AUSOT track waypoints");
			Collection<OceanicTrack> oTracks = new ArrayList<OceanicTrack>();
			for (Iterator<Map.Entry<String, Collection<String>>> i = trackData.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry<String, Collection<String>> e = i.next();
				OceanicTrack ot = new OceanicTrack(OceanicTrackInfo.Type.AUSOT, e.getKey());
				ot.setDate(or.getDate());
				
				// Calculate the location of the waypoint
				GeoLocation lastLoc = new GeoPosition(ctr);
				for (Iterator<String> wi = e.getValue().iterator(); wi.hasNext(); ) {
					String code = wi.next();
					NavigationDataBean ndb = ndmap.get(code, lastLoc);
					if ((ndb != null) && (ctr.distanceTo(ndb) < 2000)) {
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
			for (OceanicTrack ot : oTracks)
				wdao.write(ot);
			
			ctx.commitTX();
		} catch (IOException ie) {
			log.error("Error downloading AUSOT Tracks - " + ie.getMessage(), ie);
		} catch (Exception e) {
			ctx.rollbackTX();
			log.error("Error saving AUSOT Data - " + e.getMessage(), e);
		} finally {
			ctx.release();
		}
	}
}