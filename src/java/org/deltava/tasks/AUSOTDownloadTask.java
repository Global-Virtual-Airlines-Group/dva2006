// Copyright 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.net.*;
import java.util.*;
import java.io.IOException;
import java.sql.Connection;
import java.security.cert.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;
import org.deltava.taskman.*;

import org.deltava.util.http.SSLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to download AUSOT data.
 * @author Luke
 * @version 2.7
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
	protected void execute(TaskContext ctx) {
		try {
			
			// Create the URL connection to the PACOT Download side
			URL url = new URL(SystemData.get("config.ausot.url"));
			
			// Build the oceanic route bean
			OceanicNOTAM or = new OceanicNOTAM(OceanicTrackInfo.Type.AUSOT, new Date());
			or.setSource(url.getHost());
			
			// Load a key store if necessary
			String keyStore = SystemData.get("config.ausot.keystore");

			// Get the DAO and the AUSOT data
			log.info("Loading AUSOT track data from " + url.toString());
			GetAUSOTs dao = new GetAUSOTs(url.toExternalForm());
			if (("https".equals(url.getProtocol())) && (keyStore != null)) {
				log.info("Loading custom SSL keystore " + keyStore);
				X509Certificate cert = SSLUtils.load(keyStore);
				dao.setSSLContext(SSLUtils.getContext(cert));
			}
			
			// Get the waypoint data
			or.setRoute(dao.getTrackInfo());
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
			log.info("Building AUSOT track waypoints");
			Collection<OceanicTrack> oTracks = new ArrayList<OceanicTrack>();
			for (Iterator<Map.Entry<String, Collection<String>>> i = trackData.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry<String, Collection<String>> e = i.next();
				OceanicTrack ot = new OceanicTrack(OceanicTrackInfo.Type.AUSOT, e.getKey());
				ot.setDate(or.getDate());
				
				// Calculate the location of the waypoint
				GeoLocation lastLoc = new GeoPosition(-26, 133);
				for (Iterator<String> wi = e.getValue().iterator(); wi.hasNext(); ) {
					String code = wi.next();
					NavigationDataBean ndb = (lastLoc == null) ? ndmap.get(code) : ndmap.get(code, lastLoc);
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
			
			// Commit
			ctx.commitTX();
		} catch (CertificateException ce) {
			log.error("Cannot load SSL certificate - " + ce.getMessage());
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