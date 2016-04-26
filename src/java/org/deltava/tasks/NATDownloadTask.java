// Copyright 2002, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
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
 * A Scheduled Task to download Oceanic Track data.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class NATDownloadTask extends Task {

	/**
	 * Initializes the Scheduled Task.
	 */
	public NATDownloadTask() {
		super("NAT Download", NATDownloadTask.class);
	}

	/**
	 * Executes the task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		try {
			URL url = new URL(SystemData.get("config.nat.url"));
			
			// Build the oceanic route bean
			OceanicNOTAM or = new OceanicNOTAM(OceanicTrackInfo.Type.NAT, Instant.now());
			or.setSource(url.getHost());
			
			// Load waypoint data, retry up to 3 times
			log.info("Loading NAT track data from " + url.toString());
			GetNATs dao = new GetNATs(url.toExternalForm());
			int retryCount = 0; boolean isDownloaded = false;
			while (!isDownloaded && (retryCount < 3)) {
				try {
					or.setRoute(dao.getTrackInfo());		
					isDownloaded = true;
				} catch (DAOException de) {
					dao.setConnectTimeout(5000);
					dao.reset();
					retryCount++;
					log.warn("Error downloading NAT Data - " + de.getMessage());
				}
			}
			
			// Get the waypoint data
			Map<String, Collection<String>> trackData = dao.getWaypoints();
			Collection<String> waypointIDs = new HashSet<String>();
			for (Iterator<Collection<String>> i = trackData.values().iterator(); i.hasNext(); ) {
				Collection<String> waypoints = i.next();
				waypointIDs.addAll(waypoints);
			}

			// Get the connection
			Connection con = ctx.getConnection();
			
			// Get the intersection navdata values
			GetNavData nddao = new GetNavData(con);
			NavigationDataMap ndmap = nddao.getByID(waypointIDs);
			
			// Build the Route waypoints
			log.info("Building NAT track waypoints");
			Collection<OceanicTrack> oTracks = new ArrayList<OceanicTrack>();
			for (Iterator<Map.Entry<String, Collection<String>>> i = trackData.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry<String, Collection<String>> e = i.next();
				OceanicTrack ot = new OceanicTrack(OceanicTrackInfo.Type.NAT, e.getKey());
				ot.setDate(or.getDate());
				
				// Calculate the location of the waypoint
				GeoLocation lastLoc = new GeoPosition(42, -30);
				for (Iterator<String> wi = e.getValue().iterator(); wi.hasNext(); ) {
					String code = wi.next();
					NavigationDataBean ndb = ndmap.get(code, lastLoc);
					if ((ndb == null) && (code.indexOf('/') > -1))
						ndb = Intersection.parse(code);
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
			log.error("Error downloading NAT Tracks - " + ie.getMessage(), ie);
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error("Error saving NAT Data - " + de.getMessage(), de);
		} finally {
			ctx.release();
		}
	}
}